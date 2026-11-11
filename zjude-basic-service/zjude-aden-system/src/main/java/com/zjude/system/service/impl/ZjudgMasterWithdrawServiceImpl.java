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
import com.zjude.system.domain.ZjudgMasterWithdraw;
import com.zjude.system.mapper.ZjudgMasterWalletMapper;
import com.zjude.system.mapper.ZjudgMasterWithdrawMapper;
import com.zjude.system.service.ISysConfigService;
import com.zjude.system.service.IZjudgWithdrawPayer;
import com.zjude.system.service.IZjudgMasterWithdrawService;

/**
 * 师傅提现服务实现。
 *
 * <p>金额全程使用「分」为单位的 long，避免浮点误差；
 * 配置项里以「元」为单位的部分在此处统一换算。</p>
 */
@Service("zjudgMasterWithdrawService")
public class ZjudgMasterWithdrawServiceImpl implements IZjudgMasterWithdrawService
{
    private static final Logger log = LoggerFactory.getLogger(ZjudgMasterWithdrawServiceImpl.class);

    /** 最低提现金额配置（元） */
    private static final String CONFIG_MIN_WITHDRAW = "zjudg.settle.minWithdraw";

    /** 提现手续费比例配置 */
    private static final String CONFIG_FEE_RATE = "zjudg.settle.withdrawFeeRate";

    /** 最低提现金额兜底值（元） */
    private static final BigDecimal FALLBACK_MIN_WITHDRAW = new BigDecimal("10");

    /** 提现主键前缀 */
    private static final String WITHDRAW_KEY_PREFIX = "ZT_";

    /** 流水主键前缀 */
    private static final String LOG_KEY_PREFIX = "ZL_";

    @Resource
    private ZjudgMasterWithdrawMapper withdrawMapper;

    @Resource
    private ZjudgMasterWalletMapper walletMapper;

    /**
     * 打款执行器。为 null 时退化为纯手工模式（如单元测试或未部署 admin 模块）。
     */
    @Resource
    private org.springframework.beans.factory.ObjectProvider<IZjudgWithdrawPayer> payerProvider;

    @Resource
    private ISysConfigService configService;

    @Resource
    private BaseKeyGenerate keyGenerate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZjudgMasterWithdraw apply(String masterId, long amountFen, String payAccount)
    {
        if (StringUtils.isEmpty(masterId))
        {
            throw new ServiceException("请先完成师傅入驻");
        }
        if (amountFen <= 0)
        {
            throw new ServiceException("提现金额必须大于 0");
        }
        long minFen = resolveMinWithdrawFen();
        if (amountFen < minFen)
        {
            throw new ServiceException("单次提现金额不能低于 " + fenToYuan(minFen) + " 元");
        }
        ZjudgMasterWithdraw existing = withdrawMapper.selectInProgress(masterId);
        if (existing != null)
        {
            throw new ServiceException("您有一笔提现正在处理中，请等待完成后再申请");
        }

        // 冻结余额：条件里带 balance >= amount，余额不足时返回 0 行
        int frozen = walletMapper.freezeForWithdraw(masterId, amountFen);
        if (frozen == 0)
        {
            throw new ServiceException("可用余额不足");
        }

        long fee = calculateFee(amountFen);
        long actual = amountFen - fee;

        ZjudgMasterWithdraw withdraw = new ZjudgMasterWithdraw();
        withdraw.setWithdrawId(keyGenerate.snowflakeStringKey(WITHDRAW_KEY_PREFIX));
        withdraw.setWithdrawNo(withdraw.getWithdrawId());
        withdraw.setMasterId(masterId);
        withdraw.setAmount(amountFen);
        withdraw.setFee(fee);
        withdraw.setActualAmount(actual);
        withdraw.setWithdrawStatus(ZjudgMasterWithdraw.STATUS_PENDING);
        withdraw.setPayChannel("wechat");
        withdraw.setPayAccount(maskAccount(payAccount));
        withdrawMapper.insert(withdraw);

        ZjudgMasterWallet wallet = walletMapper.selectByMasterId(masterId);
        ZjudgMasterWalletLog walletLog = new ZjudgMasterWalletLog();
        walletLog.setLogId(keyGenerate.snowflakeStringKey(LOG_KEY_PREFIX));
        walletLog.setMasterId(masterId);
        walletLog.setWalletId(wallet == null ? null : wallet.getWalletId());
        walletLog.setBizType(ZjudgMasterWalletLog.TYPE_WITHDRAW);
        walletLog.setAmount(-amountFen);
        walletLog.setBalanceAfter(wallet == null ? 0L : wallet.getBalance());
        walletLog.setBizId(withdraw.getWithdrawId());
        walletLog.setBizNo(withdraw.getWithdrawNo());
        walletLog.setRemark("提现申请冻结");
        walletMapper.insertLog(walletLog);

        log.info("提现申请已提交 masterId={} amount={} withdrawNo={}", masterId, amountFen, withdraw.getWithdrawNo());
        return withdrawMapper.selectById(withdraw.getWithdrawId());
    }

    @Override
    public List<ZjudgMasterWithdraw> listByMaster(String masterId)
    {
        if (StringUtils.isEmpty(masterId))
        {
            return List.of();
        }
        return withdrawMapper.selectByMaster(masterId);
    }

    @Override
    public List<ZjudgMasterWithdraw> selectList(ZjudgMasterWithdraw query)
    {
        return withdrawMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approve(String withdrawId, String operator, String remark)
    {
        ZjudgMasterWithdraw withdraw = requireWithdraw(withdrawId);
        if (!ZjudgMasterWithdraw.STATUS_PENDING.equals(withdraw.getWithdrawStatus()))
        {
            throw new ServiceException("该提现单不是待审核状态，请刷新后重试");
        }
        int rows = withdrawMapper.updateStatus(withdrawId, ZjudgMasterWithdraw.STATUS_PENDING,
                ZjudgMasterWithdraw.STATUS_APPROVED, operator, remark, null, null);
        if (rows == 0)
        {
            throw new ServiceException("提现单状态已变更，请刷新后重试");
        }

        // 开启自动转账时审核即打款；未开启则保持「待打款」，由运营手工转账后回填单号
        IZjudgWithdrawPayer payer = payerProvider.getIfAvailable();
        if (payer != null && payer.autoEnabled())
        {
            ZjudgMasterWithdraw latest = withdrawMapper.selectById(withdrawId);
            IZjudgWithdrawPayer.PayoutResult result = payer.payout(latest);
            if (result == null)
            {
                return rows;
            }
            if (result.pending())
            {
                // 微信处理中：状态保持待打款，等回调或主动查询确认
                log.info("提现打款处理中 withdrawNo={} transferNo={}", latest.getWithdrawNo(), result.transferNo());
                return rows;
            }
            if (result.success())
            {
                markPaidInternal(latest, result.transferNo(), operator);
            }
            else
            {
                // 打款失败：状态置为失败并退回冻结金额
                markFailedInternal(latest, result.reason(), operator);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reject(String withdrawId, String operator, String reason)
    {
        if (StringUtils.isEmpty(reason))
        {
            throw new ServiceException("驳回提现时必须填写原因");
        }
        ZjudgMasterWithdraw withdraw = requireWithdraw(withdrawId);
        if (!ZjudgMasterWithdraw.STATUS_PENDING.equals(withdraw.getWithdrawStatus()))
        {
            throw new ServiceException("该提现单不是待审核状态，请刷新后重试");
        }
        int rows = withdrawMapper.updateStatus(withdrawId, ZjudgMasterWithdraw.STATUS_PENDING,
                ZjudgMasterWithdraw.STATUS_REJECTED, operator, null, null, truncate(reason, 200));
        if (rows == 0)
        {
            throw new ServiceException("提现单状态已变更，请刷新后重试");
        }
        refundFrozen(withdraw, "提现驳回退回");
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markPaid(String withdrawId, String transferNo, String operator)
    {
        ZjudgMasterWithdraw withdraw = requireWithdraw(withdrawId);
        if (!ZjudgMasterWithdraw.STATUS_APPROVED.equals(withdraw.getWithdrawStatus()))
        {
            throw new ServiceException("仅已通过的提现单可以标记打款");
        }
        markPaidInternal(withdraw, transferNo, operator);
        return 1;
    }

    /**
     * 打款成功的内核：状态流转 + 冻结扣减 + 流水。
     *
     * <p>被手工打款与自动转账两条路径共用，保证账务处理一致。</p>
     *
     * @param withdraw 提现单
     * @param transferNo 打款单号
     * @param operator 操作人
     */
    private void markPaidInternal(ZjudgMasterWithdraw withdraw, String transferNo, String operator)
    {
        int rows = withdrawMapper.updateStatus(withdraw.getWithdrawId(), ZjudgMasterWithdraw.STATUS_APPROVED,
                ZjudgMasterWithdraw.STATUS_PAID, operator, null, transferNo, null);
        if (rows == 0)
        {
            throw new ServiceException("提现单状态已变更，请刷新后重试");
        }
        // 打款成功：冻结转为已提现
        int confirmed = walletMapper.confirmWithdraw(withdraw.getMasterId(), withdraw.getAmount());
        if (confirmed == 0)
        {
            // 冻结金额不足属于数据异常，回滚整个事务避免账实不符
            throw new ServiceException("钱包冻结金额异常，请联系技术支持");
        }
        ZjudgMasterWallet wallet = walletMapper.selectByMasterId(withdraw.getMasterId());
        ZjudgMasterWalletLog walletLog = new ZjudgMasterWalletLog();
        walletLog.setLogId(keyGenerate.snowflakeStringKey(LOG_KEY_PREFIX));
        walletLog.setMasterId(withdraw.getMasterId());
        walletLog.setWalletId(wallet == null ? null : wallet.getWalletId());
        walletLog.setBizType(ZjudgMasterWalletLog.TYPE_WITHDRAW);
        walletLog.setAmount(0L);
        walletLog.setBalanceAfter(wallet == null ? 0L : wallet.getBalance());
        walletLog.setBizId(withdraw.getWithdrawId() + "_paid");
        walletLog.setBizNo(withdraw.getWithdrawNo());
        walletLog.setRemark("提现已打款 " + fenToYuan(withdraw.getActualAmount()) + " 元");
        walletMapper.insertLog(walletLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markFailed(String withdrawId, String reason, String operator)
    {
        ZjudgMasterWithdraw withdraw = requireWithdraw(withdrawId);
        if (!ZjudgMasterWithdraw.STATUS_APPROVED.equals(withdraw.getWithdrawStatus()))
        {
            throw new ServiceException("仅已通过的提现单可以标记打款失败");
        }
        markFailedInternal(withdraw, reason, operator);
        return 1;
    }

    /**
     * 打款失败的内核：状态流转 + 冻结退回 + 流水。
     *
     * @param withdraw 提现单
     * @param reason 失败原因
     * @param operator 操作人
     */
    private void markFailedInternal(ZjudgMasterWithdraw withdraw, String reason, String operator)
    {
        int rows = withdrawMapper.updateStatus(withdraw.getWithdrawId(), ZjudgMasterWithdraw.STATUS_APPROVED,
                ZjudgMasterWithdraw.STATUS_FAILED, operator, null, null, truncate(reason, 200));
        if (rows == 0)
        {
            throw new ServiceException("提现单状态已变更，请刷新后重试");
        }
        refundFrozen(withdraw, "打款失败退回");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleTransferNotify(String outBillNo, String transferBillNo, boolean success, String reason)
    {
        if (StringUtils.isEmpty(outBillNo))
        {
            return false;
        }
        // 转账单号与提现单号一致，可直接按单号定位
        ZjudgMasterWithdraw withdraw = withdrawMapper.selectByWithdrawNo(outBillNo);
        if (withdraw == null)
        {
            log.warn("转账回调找不到提现单 outBillNo={}", outBillNo);
            return false;
        }
        if (ZjudgMasterWithdraw.STATUS_PAID.equals(withdraw.getWithdrawStatus()))
        {
            // 幂等：已打款直接返回成功，避免微信重复通知
            return true;
        }
        if (!ZjudgMasterWithdraw.STATUS_APPROVED.equals(withdraw.getWithdrawStatus()))
        {
            log.info("转账回调命中非待打款状态，忽略 withdrawNo={} status={}", outBillNo, withdraw.getWithdrawStatus());
            return true;
        }
        if (success)
        {
            markPaidInternal(withdraw, transferBillNo == null ? outBillNo : transferBillNo, "wechat");
        }
        else
        {
            markFailedInternal(withdraw, StringUtils.isEmpty(reason) ? "微信转账失败" : reason, "wechat");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZjudgMasterWithdraw syncTransfer(String withdrawId)
    {
        ZjudgMasterWithdraw withdraw = requireWithdraw(withdrawId);
        if (!ZjudgMasterWithdraw.STATUS_APPROVED.equals(withdraw.getWithdrawStatus()))
        {
            return withdraw;
        }
        IZjudgWithdrawPayer payer = payerProvider.getIfAvailable();
        if (payer == null)
        {
            return withdraw;
        }
        IZjudgWithdrawPayer.PayoutResult result = payer.query(withdraw);
        if (result == null || result.pending())
        {
            return withdraw;
        }
        if (result.success())
        {
            markPaidInternal(withdraw, result.transferNo(), "system");
        }
        else
        {
            markFailedInternal(withdraw, result.reason(), "system");
        }
        return withdrawMapper.selectById(withdrawId);
    }

    /**
     * 冻结金额退回可用余额，并写流水。
     *
     * @param withdraw 提现单
     * @param remark 备注
     */
    private void refundFrozen(ZjudgMasterWithdraw withdraw, String remark)
    {
        int rows = walletMapper.refundWithdraw(withdraw.getMasterId(), withdraw.getAmount());
        if (rows == 0)
        {
            throw new ServiceException("钱包冻结金额异常，请联系技术支持");
        }
        ZjudgMasterWallet wallet = walletMapper.selectByMasterId(withdraw.getMasterId());
        ZjudgMasterWalletLog walletLog = new ZjudgMasterWalletLog();
        walletLog.setLogId(keyGenerate.snowflakeStringKey(LOG_KEY_PREFIX));
        walletLog.setMasterId(withdraw.getMasterId());
        walletLog.setWalletId(wallet == null ? null : wallet.getWalletId());
        walletLog.setBizType(ZjudgMasterWalletLog.TYPE_WITHDRAW_REFUND);
        walletLog.setAmount(withdraw.getAmount());
        walletLog.setBalanceAfter(wallet == null ? 0L : wallet.getBalance());
        walletLog.setBizId(withdraw.getWithdrawId());
        walletLog.setBizNo(withdraw.getWithdrawNo());
        walletLog.setRemark(remark);
        walletMapper.insertLog(walletLog);
    }

    /**
     * 查询提现单，不存在时抛异常。
     *
     * @param withdrawId 提现主键
     * @return 提现单
     */
    private ZjudgMasterWithdraw requireWithdraw(String withdrawId)
    {
        ZjudgMasterWithdraw withdraw = withdrawMapper.selectById(withdrawId);
        if (withdraw == null)
        {
            throw new ServiceException("提现单不存在");
        }
        return withdraw;
    }

    /**
     * 计算手续费（分）。
     *
     * @param amountFen 提现金额（分）
     * @return 手续费（分）
     */
    private long calculateFee(long amountFen)
    {
        String configured = configService.selectConfigByKey(CONFIG_FEE_RATE);
        if (StringUtils.isEmpty(configured))
        {
            return 0L;
        }
        BigDecimal rate;
        try
        {
            rate = new BigDecimal(configured.trim());
        }
        catch (NumberFormatException exception)
        {
            log.warn("提现手续费比例配置格式错误，按 0 处理 value={}", configured);
            return 0L;
        }
        if (rate.signum() <= 0)
        {
            return 0L;
        }
        if (rate.compareTo(BigDecimal.ONE) > 0)
        {
            rate = BigDecimal.ONE;
        }
        return BigDecimal.valueOf(amountFen).multiply(rate)
                .setScale(0, RoundingMode.HALF_UP).longValue();
    }

    /**
     * 读取最低提现金额（分）。
     *
     * @return 最低金额（分）
     */
    private long resolveMinWithdrawFen()
    {
        String configured = configService.selectConfigByKey(CONFIG_MIN_WITHDRAW);
        BigDecimal yuan = FALLBACK_MIN_WITHDRAW;
        if (StringUtils.isNotEmpty(configured))
        {
            try
            {
                yuan = new BigDecimal(configured.trim());
            }
            catch (NumberFormatException exception)
            {
                log.warn("最低提现金额配置格式错误，使用默认值 value={}", configured);
            }
        }
        if (yuan.signum() <= 0)
        {
            return 0L;
        }
        return yuan.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    /**
     * 分转元，用于提示文案。
     *
     * @param fen 分
     * @return 元
     */
    private String fenToYuan(long fen)
    {
        return BigDecimal.valueOf(fen).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * 收款账号脱敏，只保留后 4 位。
     *
     * @param account 原始账号
     * @return 脱敏账号
     */
    private String maskAccount(String account)
    {
        if (StringUtils.isEmpty(account))
        {
            return null;
        }
        String trimmed = account.trim();
        if (trimmed.length() <= 4)
        {
            return "****";
        }
        return "****" + trimmed.substring(trimmed.length() - 4);
    }

    private String truncate(String value, int max)
    {
        if (value == null)
        {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }
}
