package com.zjude.web.service.impl;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.integrated.payment.PaymentConfig;
import com.zjude.integrated.payment.PaymentConfigService;
import com.zjude.integrated.payment.PaymentProvider;
import com.zjude.integrated.payment.WechatPayClient;
import com.zjude.system.domain.ZjudgOrder;
import com.zjude.system.domain.ZjudgOrderPayment;
import com.zjude.system.mapper.SysUserMapper;
import com.zjude.system.mapper.ZjudgOrderMapper;
import com.zjude.system.mapper.ZjudgOrderPaymentMapper;
import com.zjude.system.service.IZjudgDispatchService;
import com.zjude.system.service.IZjudgNotifyService;
import com.zjude.system.service.IZjudgOrderPaymentService;

/**
 * 订单支付服务实现。
 *
 * <p>安全要点：</p>
 * <ul>
 *   <li>回调必须校验来源：配置了平台证书时严格验签，未配置时改走主动查单，
 *       绝不直接信任回调报文；</li>
 *   <li>金额以数据库订单金额为准，不信任回调中的金额；</li>
 *   <li>回写使用「仅未支付时更新」的幂等语句，重复回调不会重复处理。</li>
 * </ul>
 */
@Service("zjudgOrderPaymentService")
public class ZjudgOrderPaymentServiceImpl implements IZjudgOrderPaymentService
{
    private static final Logger log = LoggerFactory.getLogger(ZjudgOrderPaymentServiceImpl.class);

    /** 支付流水主键前缀 */
    private static final String PAYMENT_KEY_PREFIX = "ZP_";

    /** 微信支付成功状态 */
    private static final String TRADE_SUCCESS = "SUCCESS";

    @Resource
    private ZjudgOrderMapper orderMapper;

    @Resource
    private ZjudgOrderPaymentMapper paymentMapper;

    @Resource
    private SysUserMapper userMapper;

    @Resource
    private WechatPayClient wechatPayClient;

    @Resource
    private PaymentConfigService paymentConfigService;

    @Resource
    private IZjudgNotifyService notifyService;

    @Resource
    private WechatNotifyVerifier notifyVerifier;

    @Resource
    private IZjudgDispatchService dispatchService;

    @Resource
    private BaseKeyGenerate keyGenerate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPayment(String orderId, String userId)
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
        if ("1".equals(order.getPaymentStatus()))
        {
            throw new ServiceException("订单已支付，请勿重复支付");
        }
        if ("2".equals(order.getPaymentStatus()))
        {
            throw new ServiceException("订单已退款，无法再次支付");
        }
        if ("3".equals(order.getOrderStatus()))
        {
            throw new ServiceException("订单已取消，无法支付");
        }
        // 待接单、已接单均允许补缴订单款项；服务中及终态订单不再开放支付。
        // 已接单场景用于师傅先接单、用户后支付的订单，避免订单进入履约后无法完成支付。
        if (!"0".equals(order.getOrderStatus()) && !"4".equals(order.getOrderStatus()))
        {
            throw new ServiceException("当前订单状态不支持支付");
        }
        if (order.getAmount() == null || order.getAmount().signum() <= 0)
        {
            throw new ServiceException("订单金额异常，暂无法支付");
        }

        var user = userMapper.selectUserById(userId);
        if (user == null || StringUtils.isEmpty(user.getWxOpenid()))
        {
            throw new ServiceException("未获取到微信身份，请重新登录后再支付");
        }

        // 复用最近一条待支付流水，避免同一订单产生多条未完成流水
        ZjudgOrderPayment payment = paymentMapper.selectPendingByOrderId(orderId);
        if (payment == null)
        {
            payment = new ZjudgOrderPayment();
            payment.setPaymentId(keyGenerate.snowflakeStringKey(PAYMENT_KEY_PREFIX));
            payment.setOrderId(order.getOrderId());
            payment.setOrderNo(order.getOrderNo());
            payment.setProvider("wechat");
            payment.setOutTradeNo(order.getOrderNo());
            payment.setAmount(order.getAmount());
            payment.setPayStatus(ZjudgOrderPayment.STATUS_PENDING);
            paymentMapper.insert(payment);
        }

        Map<String, Object> payParams = wechatPayClient.createJsapiOrder(
                order.getServiceName(), payment.getOutTradeNo(), order.getAmount(), user.getWxOpenid());
        Object prepayId = payParams.get("prepayId");
        if (prepayId != null)
        {
            paymentMapper.updatePrepayId(payment.getOutTradeNo(), String.valueOf(prepayId));
        }
        return payParams;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleNotify(String body, String serial, String nonce, String signature, String timestamp)
    {
        if (StringUtils.isEmpty(body))
        {
            throw new ServiceException("回调报文为空");
        }
        PaymentConfig config = paymentConfigService.get(PaymentProvider.WECHAT);
        boolean verified = notifyVerifier.verify(config, body, serial, nonce, signature, timestamp);
        if (!verified)
        {
            log.warn("微信支付回调验签失败，改用主动查单确认 serial={}", serial);
        }

        JSONObject payload = JSON.parseObject(body);
        JSONObject resource = payload == null ? null : payload.getJSONObject("resource");
        if (resource == null)
        {
            throw new ServiceException("回调报文缺少 resource");
        }
        String plain = verified ? notifyVerifier.decrypt(config, resource) : null;
        if (StringUtils.isEmpty(plain))
        {
            // 未通过验签：不解密报文，改为按商户单号主动查询，以微信侧结果为准
            String outTradeNo = extractOutTradeNoFallback(resource);
            return confirmByQuery(outTradeNo);
        }

        JSONObject trade = JSON.parseObject(plain);
        String outTradeNo = trade.getString("out_trade_no");
        String tradeState = trade.getString("trade_state");
        String transactionId = trade.getString("transaction_id");
        if (!TRADE_SUCCESS.equals(tradeState))
        {
            log.info("微信支付回调非成功状态 outTradeNo={} state={}", outTradeNo, tradeState);
            return false;
        }
        return confirmPaid(outTradeNo, transactionId, plain);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZjudgOrder syncPaymentStatus(String orderId, String userId)
    {
        ZjudgOrder order = orderMapper.selectByIdAndUser(orderId, userId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if ("1".equals(order.getPaymentStatus()))
        {
            return order;
        }
        ZjudgOrderPayment payment = paymentMapper.selectPendingByOrderId(orderId);
        if (payment == null)
        {
            return order;
        }
        try
        {
            confirmByQuery(payment.getOutTradeNo());
        }
        catch (Exception exception)
        {
            log.warn("主动查单失败 orderId={}", orderId, exception);
        }
        ZjudgOrder latest = orderMapper.selectById(orderId);
        return latest == null ? order : latest;
    }

    /**
     * 按商户订单号查单，并以查询结果确认支付。
     *
     * @param outTradeNo 商户订单号
     * @return 是否已确认支付成功
     */
    private boolean confirmByQuery(String outTradeNo)
    {
        if (StringUtils.isEmpty(outTradeNo))
        {
            throw new ServiceException("回调报文无法确定商户订单号");
        }
        JSONObject result = wechatPayClient.queryByOutTradeNo(outTradeNo);
        String tradeState = result == null ? null : result.getString("trade_state");
        if (!TRADE_SUCCESS.equals(tradeState))
        {
            log.info("主动查单未支付 outTradeNo={} state={}", outTradeNo, tradeState);
            return false;
        }
        String transactionId = result.getString("transaction_id");
        return confirmPaid(outTradeNo, transactionId, null);
    }

    /**
     * 幂等确认支付成功：更新流水与订单，并发送支付成功通知。
     *
     * @param outTradeNo 商户订单号
     * @param transactionId 微信交易号
     * @param raw 回调明文，可为空
     * @return 是否首次确认成功
     */
    private boolean confirmPaid(String outTradeNo, String transactionId, String raw)
    {
        ZjudgOrderPayment payment = paymentMapper.selectByOutTradeNo(outTradeNo);
        if (payment == null)
        {
            log.warn("支付回调找不到流水 outTradeNo={}", outTradeNo);
            return false;
        }
        int paymentRows = paymentMapper.markPaid(outTradeNo, transactionId, truncateRaw(raw));
        if (paymentRows == 0)
        {
            // 已经处理过，幂等返回成功，避免微信重复重试
            log.info("支付回调重复，已忽略 outTradeNo={}", outTradeNo);
            return true;
        }
        int orderRows = orderMapper.markPaid(payment.getOrderId(), "wechat");
        if (orderRows > 0)
        {
            notifyService.notifyPaid(orderMapper.selectById(payment.getOrderId()));
            // 支付成功后触发自动派单；失败不影响支付结果，由定时任务或后台补派
            try
            {
                dispatchService.autoDispatch(payment.getOrderId());
            }
            catch (Exception exception)
            {
                log.warn("支付成功后自动派单失败 orderId={}", payment.getOrderId(), exception);
            }
        }
        return true;
    }

    /**
     * 验签失败时，从 resource 中尽力还原商户订单号。
     *
     * <p>密文无法解析时返回空串，由调用方按业务订单号查单兜底。</p>
     *
     * @param resource 回调 resource 节点
     * @return 商户订单号或空串
     */
    private String extractOutTradeNoFallback(JSONObject resource)
    {
        String outTradeNo = resource.getString("out_trade_no");
        return outTradeNo == null ? "" : outTradeNo;
    }

    /**
     * 截断回调报文，避免超长导致落库失败。
     *
     * @param raw 回调明文
     * @return 截断后的报文
     */
    private String truncateRaw(String raw)
    {
        if (raw == null)
        {
            return null;
        }
        int max = 2000;
        return raw.length() <= max ? raw : raw.substring(0, max);
    }
}
