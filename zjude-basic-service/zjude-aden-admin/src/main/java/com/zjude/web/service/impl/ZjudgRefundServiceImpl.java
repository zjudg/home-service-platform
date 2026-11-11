package com.zjude.web.service.impl;

import java.math.BigDecimal;
import java.util.List;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSONObject;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.integrated.payment.WechatPayClient;
import com.zjude.system.domain.ZjudgOrder;
import com.zjude.system.domain.ZjudgRefund;
import com.zjude.system.mapper.ZjudgOrderMapper;
import com.zjude.system.mapper.ZjudgRefundMapper;
import com.zjude.system.service.IZjudgRefundService;
import com.zjude.system.service.IZjudgSettleService;

/**
 * 订单退款服务实现。
 *
 * <p>资金安全相关的不变量：</p>
 * <ul>
 *   <li>仅「已支付」订单可退款，未支付、已退款、已取消未付款都不允许；</li>
 *   <li>退款金额以订单实付金额为准，不接受客户端指定；</li>
 *   <li>同一订单同时只允许一条进行中的退款单，避免重复退款；</li>
 *   <li>状态回写使用「仅当状态匹配时更新」，重复回调不会重复退款。</li>
 * </ul>
 */
@Service("zjudgRefundService")
public class ZjudgRefundServiceImpl implements IZjudgRefundService
{
    private static final Logger log = LoggerFactory.getLogger(ZjudgRefundServiceImpl.class);

    /** 退款单主键前缀 */
    private static final String REFUND_KEY_PREFIX = "ZR_";

    /** 微信退款成功状态 */
    private static final String WX_REFUND_SUCCESS = "SUCCESS";

    /** 微信退款处理中 */
    private static final String WX_REFUND_PROCESSING = "PROCESSING";

    @Resource
    private ZjudgRefundMapper refundMapper;

    @Resource
    private ZjudgOrderMapper orderMapper;

    @Resource
    private IZjudgSettleService settleService;

    @Resource
    private WechatPayClient wechatPayClient;

    @Resource
    private BaseKeyGenerate keyGenerate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZjudgRefund applyRefund(String orderId, String userId, String reason)
    {
        if (StringUtils.isEmpty(userId))
        {
            throw new ServiceException("请先登录");
        }
        ZjudgOrder order = orderMapper.selectByIdAndUser(orderId, userId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (!"1".equals(order.getPaymentStatus()))
        {
            throw new ServiceException("当前订单状态不支持退款");
        }
        // 已完成服务通常不再支持自助退款，由平台人工介入
        if ("2".equals(order.getOrderStatus()))
        {
            throw new ServiceException("服务已完成，如需退款请联系客服");
        }
        BigDecimal amount = refundableAmount(orderId);
        if (amount.signum() <= 0)
        {
            throw new ServiceException("该订单没有可退款金额");
        }
        ZjudgRefund latest = refundMapper.selectLatestByOrderId(orderId);
        if (latest != null && latest.isInProgress())
        {
            throw new ServiceException("该订单已有退款申请正在处理中");
        }
        if (latest != null && ZjudgRefund.STATUS_SUCCESS.equals(latest.getRefundStatus()))
        {
            throw new ServiceException("该订单已退款完成");
        }

        ZjudgRefund refund = new ZjudgRefund();
        refund.setRefundId(keyGenerate.snowflakeStringKey(REFUND_KEY_PREFIX));
        // 退款单号同时作为微信 out_refund_no，保证全局唯一
        refund.setRefundNo(refund.getRefundId());
        refund.setOrderId(order.getOrderId());
        refund.setOrderNo(order.getOrderNo());
        refund.setBuyerInfo(order.getUserInfo());
        refund.setRefundAmount(amount);
        refund.setRefundType("0");
        refund.setRefundStatus(ZjudgRefund.STATUS_PENDING);
        refund.setRemark(truncate(reason, 500));
        refund.setCreateBy(userId);
        refundMapper.insert(refund);
        return refundMapper.selectById(refund.getRefundId());
    }

    @Override
    public ZjudgRefund getByOrder(String orderId, String userId)
    {
        ZjudgOrder order = orderMapper.selectByIdAndUser(orderId, userId);
        if (order == null)
        {
            // 非本人订单不暴露退款信息
            return null;
        }
        return refundMapper.selectLatestByOrderId(orderId);
    }

    @Override
    public List<ZjudgRefund> selectList(ZjudgRefund query)
    {
        return refundMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZjudgRefund approve(String refundId, String operator)
    {
        ZjudgRefund refund = refundMapper.selectById(refundId);
        if (refund == null)
        {
            throw new ServiceException("退款单不存在");
        }
        if (!ZjudgRefund.STATUS_PENDING.equals(refund.getRefundStatus()))
        {
            throw new ServiceException("该退款单不是待处理状态，请刷新后重试");
        }
        int rows = refundMapper.updateStatus(refund.getRefundNo(), ZjudgRefund.STATUS_PENDING,
                ZjudgRefund.STATUS_PROCESSING, null, null);
        if (rows == 0)
        {
            throw new ServiceException("退款单状态已变更，请刷新后重试");
        }

        ZjudgOrder order = orderMapper.selectById(refund.getOrderId());
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        // 金额以订单实付为准，不接受退款单上被篡改的金额
        BigDecimal amount = order.getAmount();
        if (amount == null || amount.signum() <= 0)
        {
            throw new ServiceException("订单金额异常，无法退款");
        }

        try
        {
            JSONObject result = wechatPayClient.createRefund(order.getOrderNo(), refund.getRefundNo(),
                    amount, amount, "用户申请退款");
            String status = result.getString("status");
            String wxRefundId = result.getString("refund_id");
            if (WX_REFUND_SUCCESS.equals(status))
            {
                confirmSuccess(refund.getRefundNo(), wxRefundId, operator);
            }
            else if (!WX_REFUND_PROCESSING.equals(status))
            {
                refundMapper.updateStatus(refund.getRefundNo(), ZjudgRefund.STATUS_PROCESSING,
                        ZjudgRefund.STATUS_FAILED, wxRefundId, "微信退款状态：" + status);
            }
            // PROCESSING 保持退款中，等回调或主动查询
        }
        catch (Exception exception)
        {
            // 提交失败要回滚为待处理，避免卡在退款中无法再次操作
            refundMapper.updateStatus(refund.getRefundNo(), ZjudgRefund.STATUS_PROCESSING,
                    ZjudgRefund.STATUS_FAILED, null, truncate(exception.getMessage(), 200));
            throw exception;
        }
        return refundMapper.selectById(refundId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reject(String refundId, String reason, String operator)
    {
        if (StringUtils.isEmpty(reason))
        {
            throw new ServiceException("拒绝退款时必须填写原因");
        }
        ZjudgRefund refund = refundMapper.selectById(refundId);
        if (refund == null)
        {
            throw new ServiceException("退款单不存在");
        }
        int rows = refundMapper.updateStatus(refund.getRefundNo(), ZjudgRefund.STATUS_PENDING,
                ZjudgRefund.STATUS_REJECTED, null, truncate(reason, 200));
        if (rows == 0)
        {
            throw new ServiceException("退款单状态已变更，请刷新后重试");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZjudgRefund syncStatus(String refundId)
    {
        ZjudgRefund refund = refundMapper.selectById(refundId);
        if (refund == null)
        {
            throw new ServiceException("退款单不存在");
        }
        if (ZjudgRefund.STATUS_SUCCESS.equals(refund.getRefundStatus())
                || ZjudgRefund.STATUS_FAILED.equals(refund.getRefundStatus())
                || ZjudgRefund.STATUS_REJECTED.equals(refund.getRefundStatus()))
        {
            return refund;
        }
        try
        {
            JSONObject result = wechatPayClient.queryRefund(refund.getRefundNo());
            String status = result == null ? null : result.getString("status");
            String wxRefundId = result == null ? null : result.getString("refund_id");
            if (WX_REFUND_SUCCESS.equals(status))
            {
                confirmSuccess(refund.getRefundNo(), wxRefundId, "system");
            }
            else if (status != null && !WX_REFUND_PROCESSING.equals(status))
            {
                refundMapper.updateStatus(refund.getRefundNo(), ZjudgRefund.STATUS_PROCESSING,
                        ZjudgRefund.STATUS_FAILED, wxRefundId, "微信退款状态：" + status);
            }
        }
        catch (Exception exception)
        {
            log.warn("退款状态同步失败 refundNo={}", refund.getRefundNo(), exception);
        }
        return refundMapper.selectById(refundId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleNotify(String refundNo, String wxRefundId, boolean success, String failReason)
    {
        if (StringUtils.isEmpty(refundNo))
        {
            return false;
        }
        if (success)
        {
            return confirmSuccess(refundNo, wxRefundId, "wechat");
        }
        int rows = refundMapper.updateStatus(refundNo, ZjudgRefund.STATUS_PROCESSING,
                ZjudgRefund.STATUS_FAILED, wxRefundId, truncate(failReason, 200));
        if (rows == 0)
        {
            // 重复回调或状态已终态，视为已处理
            log.info("退款回调重复或状态已变更 refundNo={}", refundNo);
            return true;
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmByQuery(String refundNo)
    {
        if (StringUtils.isEmpty(refundNo))
        {
            return false;
        }
        ZjudgRefund refund = refundMapper.selectByRefundNo(refundNo);
        if (refund == null)
        {
            log.warn("退款查单找不到退款单 refundNo={}", refundNo);
            return false;
        }
        if (ZjudgRefund.STATUS_SUCCESS.equals(refund.getRefundStatus()))
        {
            return true;
        }
        try
        {
            JSONObject result = wechatPayClient.queryRefund(refundNo);
            String status = result == null ? null : result.getString("status");
            String wxRefundId = result == null ? null : result.getString("refund_id");
            if (WX_REFUND_SUCCESS.equals(status))
            {
                return confirmSuccess(refundNo, wxRefundId, "wechat");
            }
            if (status != null && !WX_REFUND_PROCESSING.equals(status))
            {
                refundMapper.updateStatus(refundNo, ZjudgRefund.STATUS_PROCESSING,
                        ZjudgRefund.STATUS_FAILED, wxRefundId, "微信退款状态：" + status);
            }
            return false;
        }
        catch (Exception exception)
        {
            log.warn("退款查单失败 refundNo={}", refundNo, exception);
            return false;
        }
    }

    @Override
    public BigDecimal refundableAmount(String orderId)
    {
        ZjudgOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getAmount() == null)
        {
            return BigDecimal.ZERO;
        }
        if (!"1".equals(order.getPaymentStatus()))
        {
            return BigDecimal.ZERO;
        }
        // 已完成订单不允许退款：收入已结算给师傅，且服务已交付
        if ("2".equals(order.getOrderStatus()))
        {
            return BigDecimal.ZERO;
        }
        return order.getAmount();
    }

    /**
     * 幂等确认退款成功：更新退款单与订单支付状态。
     *
     * @param refundNo 退款单号
     * @param wxRefundId 微信退款单号
     * @param operator 操作人
     * @return 是否首次确认成功
     */
    private boolean confirmSuccess(String refundNo, String wxRefundId, String operator)
    {
        ZjudgRefund refund = refundMapper.selectByRefundNo(refundNo);
        if (refund == null)
        {
            log.warn("退款成功但找不到退款单 refundNo={}", refundNo);
            return false;
        }
        int rows = refundMapper.updateStatus(refundNo, ZjudgRefund.STATUS_PROCESSING,
                ZjudgRefund.STATUS_SUCCESS, wxRefundId, null);
        if (rows == 0)
        {
            log.info("退款重复确认，已忽略 refundNo={}", refundNo);
            return true;
        }
        orderMapper.markRefunded(refund.getOrderId(), operator);
        // 退款成功后冲回师傅已结算的收入，避免平台与师傅账目不符
        settleService.reverseOrder(refund.getOrderId(), "订单退款");
        return true;
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
