package com.zjude.system.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.ZjudgMasterWallet;
import com.zjude.system.domain.ZjudgMasterWalletLog;
import com.zjude.system.domain.ZjudgOrder;
import com.zjude.system.domain.ZjudgService;
import com.zjude.system.mapper.ZjudgMasterWalletMapper;
import com.zjude.system.mapper.ZjudgOrderMapper;
import com.zjude.system.mapper.ZjudgServiceMapper;
import com.zjude.system.service.ISysConfigService;
import com.zjude.system.service.IZjudgSettleService;

/**
 * 师傅结算服务实现。
 *
 * <p>金额精度：数据库以分为单位存整数，订单金额是元（decimal），
 * 因此统一在服务端用 {@link RoundingMode#HALF_UP} 换算成分，避免各端算法不一致。</p>
 *
 * <p>幂等保证：钱包流水表对 {@code biz_type + biz_id} 建了唯一索引，
 * 同一订单的入账/冲回都只能成功一次。</p>
 */
@Service("zjudgSettleService")
public class ZjudgSettleServiceImpl implements IZjudgSettleService
{
    private static final Logger log = LoggerFactory.getLogger(ZjudgSettleServiceImpl.class);

    /** 默认佣金比例配置键 */
    private static final String CONFIG_DEFAULT_COMMISSION = "zjudg.settle.defaultCommission";

    /** 默认佣金比例，配置缺失时使用 */
    private static final BigDecimal FALLBACK_COMMISSION = new BigDecimal("0.2");

    /** 订单结算状态：已结算 */
    private static final String SETTLE_DONE = "1";

    /** 订单结算状态：已冲回 */
    private static final String SETTLE_REVERSED = "2";

    /** 钱包主键前缀 */
    private static final String WALLET_KEY_PREFIX = "ZW_";

    /** 流水主键前缀 */
    private static final String LOG_KEY_PREFIX = "ZL_";

    @Resource
    private ZjudgMasterWalletMapper walletMapper;

    @Resource
    private ZjudgOrderMapper orderMapper;

    @Resource
    private ZjudgServiceMapper serviceMapper;

    @Resource
    private ISysConfigService configService;

    @Resource
    private BaseKeyGenerate keyGenerate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long settleOrder(String orderId)
    {
        ZjudgOrder order = orderMapper.selectById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (StringUtils.isEmpty(order.getMasterId()))
        {
            // 未派单的订单没有结算对象，属于正常情况
            return 0L;
        }
        if (SETTLE_DONE.equals(order.getSettleStatus()))
        {
            // 已结算，幂等返回
            return 0L;
        }
        if (SETTLE_REVERSED.equals(order.getSettleStatus()))
        {
            // 曾冲回过，不再重复入账
            return 0L;
        }
        if (!"1".equals(order.getPaymentStatus()))
        {
            // 未支付订单不产生师傅收入
            return 0L;
        }
        if (order.getAmount() == null || order.getAmount().signum() <= 0)
        {
            log.warn("订单金额异常，跳过结算 orderId={}", orderId);
            return 0L;
        }

        long totalFen = toFen(order.getAmount());
        long masterIncome;
        if (order.getMasterPrice() != null)
        {
            masterIncome = toFen(order.getMasterPrice());
            if (masterIncome < 0 || masterIncome > totalFen)
            {
                throw new ServiceException("订单接单价快照异常，无法结算");
            }
        }
        else
        {
            // 兼容升级前尚未补齐接单价快照的历史订单。
            BigDecimal commissionRate = resolveCommission(order.getServiceId());
            long platformFeeByRate = BigDecimal.valueOf(totalFen)
                    .multiply(commissionRate).setScale(0, RoundingMode.HALF_UP).longValue();
            masterIncome = totalFen - Math.min(Math.max(platformFeeByRate, 0L), totalFen);
        }
        long platformFee = totalFen - masterIncome;

        ensureWallet(order.getMasterId());
        int rows = walletMapper.increaseIncome(order.getMasterId(), masterIncome);
        if (rows == 0)
        {
            throw new ServiceException("结算失败：师傅钱包不存在");
        }
        ZjudgMasterWallet wallet = walletMapper.selectByMasterId(order.getMasterId());

        ZjudgMasterWalletLog walletLog = new ZjudgMasterWalletLog();
        walletLog.setLogId(keyGenerate.snowflakeStringKey(LOG_KEY_PREFIX));
        walletLog.setMasterId(order.getMasterId());
        walletLog.setWalletId(wallet.getWalletId());
        walletLog.setBizType(ZjudgMasterWalletLog.TYPE_ORDER_INCOME);
        walletLog.setAmount(masterIncome);
        walletLog.setBalanceAfter(wallet.getBalance());
        walletLog.setBizId(order.getOrderId());
        walletLog.setBizNo(order.getOrderNo());
        walletLog.setRemark("订单结算：" + order.getServiceName());
        walletMapper.insertLog(walletLog);

        orderMapper.markSettled(orderId, masterIncome, platformFee);

        log.info("订单结算完成 orderId={} masterId={} income={} fee={}", orderId, order.getMasterId(),
                masterIncome, platformFee);
        return masterIncome;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long reverseOrder(String orderId, String reason)
    {
        ZjudgOrder order = orderMapper.selectById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (!SETTLE_DONE.equals(order.getSettleStatus()))
        {
            // 未结算过，无需冲回
            return 0L;
        }
        long income = order.getMasterIncome() == null ? 0L : order.getMasterIncome();
        if (income <= 0)
        {
            orderMapper.markSettleReversed(orderId);
            return 0L;
        }

        // 余额不足时（师傅已提现）仍要冲回，允许余额为负，由后续收入抵扣
        walletMapper.decreaseIncome(order.getMasterId(), income);
        ZjudgMasterWallet wallet = walletMapper.selectByMasterId(order.getMasterId());

        ZjudgMasterWalletLog walletLog = new ZjudgMasterWalletLog();
        walletLog.setLogId(keyGenerate.snowflakeStringKey(LOG_KEY_PREFIX));
        walletLog.setMasterId(order.getMasterId());
        walletLog.setWalletId(wallet == null ? null : wallet.getWalletId());
        walletLog.setBizType(ZjudgMasterWalletLog.TYPE_ORDER_REVERSE);
        walletLog.setAmount(-income);
        walletLog.setBalanceAfter(wallet == null ? 0L : wallet.getBalance());
        walletLog.setBizId(order.getOrderId());
        walletLog.setBizNo(order.getOrderNo());
        walletLog.setRemark("订单冲回：" + (reason == null ? "订单退款" : reason));
        walletMapper.insertLog(walletLog);

        orderMapper.markSettleReversed(orderId);
        log.info("订单结算冲回 orderId={} masterId={} amount={}", orderId, order.getMasterId(), income);
        return income;
    }

    @Override
    public ZjudgMasterWallet getWallet(String masterId)
    {
        if (StringUtils.isEmpty(masterId))
        {
            throw new ServiceException("师傅主键不能为空");
        }
        ZjudgMasterWallet wallet = walletMapper.selectByMasterId(masterId);
        if (wallet != null)
        {
            return wallet;
        }
        ensureWallet(masterId);
        return walletMapper.selectByMasterId(masterId);
    }

    @Override
    public List<ZjudgMasterWalletLog> listLogs(String masterId, String bizType)
    {
        return walletMapper.selectLogs(masterId, bizType);
    }

    @Override
    public List<ZjudgMasterWalletLog> listAllLogs(ZjudgMasterWalletLog query)
    {
        return walletMapper.selectLogList(query);
    }

    /**
     * 确保钱包存在，不存在则初始化。
     *
     * @param masterId 师傅主键
     */
    private void ensureWallet(String masterId)
    {
        if (walletMapper.selectByMasterId(masterId) != null)
        {
            return;
        }
        ZjudgMasterWallet wallet = new ZjudgMasterWallet();
        wallet.setWalletId(keyGenerate.snowflakeStringKey(WALLET_KEY_PREFIX));
        wallet.setMasterId(masterId);
        try
        {
            walletMapper.insert(wallet);
        }
        catch (Exception exception)
        {
            // 并发初始化时唯一索引会冲突，说明其他线程已创建，直接忽略
            log.debug("钱包已由其他线程创建 masterId={}", masterId);
        }
    }

    /**
     * 解析佣金比例：优先取服务自身配置，缺失时取全局默认。
     *
     * @param serviceId 服务主键
     * @return 佣金比例，范围 [0,1]
     */
    private BigDecimal resolveCommission(String serviceId)
    {
        BigDecimal rate = null;
        if (StringUtils.isNotEmpty(serviceId))
        {
            ZjudgService service = serviceMapper.selectById(serviceId);
            if (service != null && service.getCommissionRate() != null)
            {
                rate = service.getCommissionRate();
            }
        }
        if (rate == null)
        {
            String configured = configService.selectConfigByKey(CONFIG_DEFAULT_COMMISSION);
            rate = parseRate(configured, FALLBACK_COMMISSION);
        }
        if (rate.signum() < 0)
        {
            return BigDecimal.ZERO;
        }
        if (rate.compareTo(BigDecimal.ONE) > 0)
        {
            return BigDecimal.ONE;
        }
        return rate;
    }

    /**
     * 安全解析比例配置。
     *
     * @param value 配置值
     * @param fallback 兜底值
     * @return 比例
     */
    private BigDecimal parseRate(String value, BigDecimal fallback)
    {
        if (StringUtils.isEmpty(value))
        {
            return fallback;
        }
        try
        {
            return new BigDecimal(value.trim());
        }
        catch (NumberFormatException exception)
        {
            log.warn("佣金比例配置格式错误，使用默认值 value={}", value);
            return fallback;
        }
    }

    /**
     * 元转分。
     *
     * @param amount 元
     * @return 分
     */
    private long toFen(BigDecimal amount)
    {
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
    }
}
