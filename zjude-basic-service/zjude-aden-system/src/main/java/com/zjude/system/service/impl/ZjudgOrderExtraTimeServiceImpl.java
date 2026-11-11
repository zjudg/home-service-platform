package com.zjude.system.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.ZjudgMasterWallet;
import com.zjude.system.domain.ZjudgMasterWalletLog;
import com.zjude.system.domain.ZjudgOrder;
import com.zjude.system.domain.ZjudgOrderExtraTime;
import com.zjude.system.domain.ZjudgService;
import com.zjude.system.domain.enums.ZjudgExtraTimePaymentStatus;
import com.zjude.system.mapper.ZjudgMasterWalletMapper;
import com.zjude.system.mapper.ZjudgOrderExtraTimeMapper;
import com.zjude.system.mapper.ZjudgOrderMapper;
import com.zjude.system.mapper.ZjudgServiceMapper;
import com.zjude.system.service.IZjudgOrderExtraTimeService;
import com.zjude.system.service.IZjudgSettleService;

@Service("zjudgOrderExtraTimeService")
public class ZjudgOrderExtraTimeServiceImpl implements IZjudgOrderExtraTimeService
{
    private static final BigDecimal MAX_HOURS = BigDecimal.valueOf(24);
    @Resource private ZjudgOrderMapper orderMapper;
    @Resource private ZjudgServiceMapper serviceMapper;
    @Resource private ZjudgOrderExtraTimeMapper extraTimeMapper;
    @Resource private ZjudgMasterWalletMapper walletMapper;
    @Resource private IZjudgSettleService settleService;
    @Resource private BaseKeyGenerate keyGenerate;

    @Override
    public ZjudgService getProductForOrder(String orderId, String userId)
    {
        ZjudgOrder order = requireOwnedOrder(orderId, userId);
        if (!"1".equals(order.getPaymentStatus())
                || (!"4".equals(order.getOrderStatus()) && !"1".equals(order.getOrderStatus()) && !"2".equals(order.getOrderStatus()))
                || StringUtils.isEmpty(order.getMasterId())) return null;
        ZjudgService original = serviceMapper.selectById(order.getServiceId());
        if (original == null || !"HOUR".equals(original.getMeasureType())) return null;
        return serviceMapper.selectExtraTimeProduct(order.getCityId());
    }

    @Override
    public List<ZjudgOrderExtraTime> listForOrder(String orderId, String userId)
    {
        requireOwnedOrder(orderId, userId);
        return extraTimeMapper.selectByOrderAndUser(orderId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZjudgOrderExtraTime create(String orderId, String userId, int hours)
    {
        if (hours < 1 || BigDecimal.valueOf(hours).compareTo(MAX_HOURS) > 0)
        {
            throw new ServiceException("补时时长必须是1至24小时的整数");
        }
        ZjudgOrder order = requireEligibleOrder(orderId, userId);
        ZjudgService product = serviceMapper.selectExtraTimeProduct(order.getCityId());
        if (product == null || product.getPrice() == null || product.getPrice().signum() <= 0 || product.getMasterPrice() == null)
        {
            throw new ServiceException("當前城市尚未配置補時專用商品");
        }
        ZjudgOrderExtraTime item = new ZjudgOrderExtraTime();
        item.setExtraTimeId(keyGenerate.snowflakeStringKey("ZET_"));
        item.setExtraTimeNo(keyGenerate.snowflakeStringKey("ZETN_"));
        item.setOrderId(order.getOrderId());
        item.setUserId(userId);
        item.setMasterId(order.getMasterId());
        item.setServiceId(product.getServiceId());
        item.setServiceName(product.getServiceName());
        item.setHours(hours);
        item.setAmount(product.getPrice().multiply(BigDecimal.valueOf(hours)).setScale(2, RoundingMode.HALF_UP));
        item.setMasterAmount(product.getMasterPrice().multiply(BigDecimal.valueOf(hours)).setScale(2, RoundingMode.HALF_UP));
        item.setPaymentStatus(ZjudgExtraTimePaymentStatus.PENDING.value());
        extraTimeMapper.insert(item);
        return item;
    }

    @Override
    public ZjudgOrderExtraTime getForUser(String extraTimeId, String userId)
    {
        if (StringUtils.isEmpty(userId)) throw new ServiceException("请先登录");
        ZjudgOrderExtraTime item = extraTimeMapper.selectByIdAndUser(extraTimeId, userId);
        if (item != null && ZjudgExtraTimePaymentStatus.PENDING.value().equals(item.getPaymentStatus()))
        {
            requireEligibleOrder(item.getOrderId(), userId);
        }
        return item;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markPaidAndSettle(String extraTimeId)
    {
        ZjudgOrderExtraTime item = extraTimeMapper.selectById(extraTimeId);
        if (item == null) throw new ServiceException("补时记录不存在");
        if (ZjudgExtraTimePaymentStatus.PAID.value().equals(item.getPaymentStatus())) return true;
        if (extraTimeMapper.markPaid(extraTimeId) == 0) return false;
        if (item.getMasterAmount() == null || item.getMasterAmount().signum() < 0
                || item.getMasterAmount().compareTo(item.getAmount()) > 0)
        {
            throw new ServiceException("补时师傅收入金额异常");
        }
        long income = toFen(item.getMasterAmount());
        settleService.getWallet(item.getMasterId());
        if (walletMapper.increaseIncome(item.getMasterId(), income) == 0)
        {
            throw new ServiceException("补时收入入账失败");
        }
        ZjudgMasterWallet wallet = walletMapper.selectByMasterId(item.getMasterId());
        ZjudgMasterWalletLog log = new ZjudgMasterWalletLog();
        log.setLogId(keyGenerate.snowflakeStringKey("ZL_"));
        log.setMasterId(item.getMasterId());
        log.setWalletId(wallet.getWalletId());
        log.setBizType(ZjudgMasterWalletLog.TYPE_EXTRA_TIME_INCOME);
        log.setAmount(income);
        log.setBalanceAfter(wallet.getBalance());
        log.setBizId(item.getExtraTimeId());
        log.setBizNo(item.getExtraTimeNo());
        log.setRemark("订单补时收入：" + item.getServiceName() + " × " + item.getHours() + "小时");
        walletMapper.insertLog(log);
        return true;
    }

    private ZjudgOrder requireEligibleOrder(String orderId, String userId)
    {
        ZjudgOrder order = requireOwnedOrder(orderId, userId);
        if (!"1".equals(order.getPaymentStatus())) throw new ServiceException("原订单支付完成后才能补时");
        if (!"4".equals(order.getOrderStatus()) && !"1".equals(order.getOrderStatus()) && !"2".equals(order.getOrderStatus()))
            throw new ServiceException("原订单当前状态不能补时");
        if (StringUtils.isEmpty(order.getMasterId())) throw new ServiceException("原订单尚未分配师傅，暂不能补时");
        ZjudgService original = serviceMapper.selectById(order.getServiceId());
        if (original == null || !"HOUR".equals(original.getMeasureType())) throw new ServiceException("仅按小时计价的订单支持补时");
        return order;
    }

    private ZjudgOrder requireOwnedOrder(String orderId, String userId)
    {
        if (StringUtils.isEmpty(userId)) throw new ServiceException("请先登录");
        ZjudgOrder order = orderMapper.selectByIdAndUser(orderId, userId);
        if (order == null) throw new ServiceException("订单不存在");
        return order;
    }

    private long toFen(BigDecimal amount)
    {
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
    }
}
