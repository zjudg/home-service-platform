package com.zjude.web.controller.mini;

import java.util.Map;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.annotation.Anonymous;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.utils.SecurityUtils;
import com.zjude.system.domain.ZjudgOrder;
import com.zjude.system.service.IZjudgOrderPaymentService;
import com.zjude.system.service.IZjudgRefundService;
import com.zjude.integrated.payment.PaymentConfig;
import com.zjude.integrated.payment.PaymentConfigService;
import com.zjude.integrated.payment.PaymentProvider;
import com.zjude.web.service.impl.WechatNotifyVerifier;

/**
 * 小程序订单支付接口。
 *
 * <p>下单与查单需要登录态；微信回调由微信服务器调用，必须匿名放行并依赖验签或主动查单保证真实性。</p>
 */
@RestController
@RequestMapping("/api/mini/pay")
public class MiniPaymentController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(MiniPaymentController.class);

    @Resource
    private IZjudgOrderPaymentService paymentService;

    @Resource
    private com.zjude.system.service.IZjudgBusinessPaymentService businessPaymentService;

    @Resource
    private IZjudgRefundService refundService;

    @Resource
    private com.zjude.system.service.IZjudgMasterWithdrawService withdrawService;

    @Resource
    private WechatNotifyVerifier verifier;

    @Resource
    private PaymentConfigService paymentConfigService;

    /**
     * 创建支付单，返回 uni.requestPayment 所需参数。
     *
     * @param orderId 订单主键
     * @return 支付参数
     */
    @PostMapping("/orders/{orderId}")
    public AjaxResult createPayment(@PathVariable String orderId)
    {
        Map<String, Object> params = paymentService.createPayment(orderId, SecurityUtils.getUserId());
        return AjaxResult.success("支付单已创建", params);
    }

    /**
     * 主动同步支付结果，供前端支付完成后轮询确认。
     *
     * @param orderId 订单主键
     * @return 最新订单
     */
    @GetMapping("/orders/{orderId}/status")
    public AjaxResult syncStatus(@PathVariable String orderId)
    {
        ZjudgOrder order = paymentService.syncPaymentStatus(orderId, SecurityUtils.getUserId());
        if (order != null)
        {
            order.setMasterPrice(null);
        }
        return AjaxResult.success(order);
    }

    /**
     * 微信支付回调。
     *
     * <p>返回体必须严格符合微信要求：成功返回 200 且无异常响应体，失败返回 4xx/5xx 让微信重试。</p>
     *
     * @param body 回调报文
     * @param serial 平台证书序列号
     * @param nonce 随机串
     * @param signature 签名
     * @param timestamp 时间戳
     * @return 处理结果
     */
    @Anonymous
    @PostMapping({"/notify", "/refund-notify"})
    public AjaxResult notify(@RequestBody(required = false) String body,
            @org.springframework.web.bind.annotation.RequestHeader(value = "Wechatpay-Serial", required = false) String serial,
            @org.springframework.web.bind.annotation.RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce,
            @org.springframework.web.bind.annotation.RequestHeader(value = "Wechatpay-Signature", required = false) String signature,
            @org.springframework.web.bind.annotation.RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp)
    {
        try
        {
            // 支付、退款、转账共用同一回调地址。event_type 位于报文顶层且不加密，
            // 因此分流判断不依赖解密是否成功。
            String eventType = eventTypeOf(body);
            if (eventType != null && eventType.startsWith("REFUND."))
            {
                handleRefundNotify(eventType, body, serial, nonce, signature, timestamp);
            }
            else if (eventType != null && eventType.startsWith("MCHTRANSFER."))
            {
                handleTransferNotify(eventType, body, serial, nonce, signature, timestamp);
            }
            else
            {
                if (!businessPaymentService.handleNotify(body, serial, nonce, signature, timestamp))
                {
                    paymentService.handleNotify(body, serial, nonce, signature, timestamp);
                }
            }
            return AjaxResult.success();
        }
        catch (Exception exception)
        {
            // 不返回具体错误细节，避免泄露内部实现；微信会按非 200 重试
            log.warn("微信回调处理失败", exception);
            return AjaxResult.error("处理失败");
        }
    }

    /**
     * 取报文顶层的事件类型。
     *
     * @param body 报文
     * @return 事件类型，无法判断时返回 null
     */
    private String eventTypeOf(String body)
    {
        if (body == null || body.isBlank())
        {
            return null;
        }
        try
        {
            com.alibaba.fastjson2.JSONObject payload = com.alibaba.fastjson2.JSON.parseObject(body);
            return payload == null ? null : payload.getString("event_type");
        }
        catch (Exception exception)
        {
            // 报文无法解析时按支付回调处理，由支付服务内部校验
            return null;
        }
    }

    /**
     * 处理退款回调。
     *
     * <p>先验签；验签不通过时不解密，改为按退款单号主动查单，
     * 与支付回调的兜底策略保持一致，避免伪造回调导致退款状态被篡改。</p>
     *
     * @param eventType 事件类型
     * @param body 报文
     * @param serial 平台证书序列号
     * @param nonce 随机串
     * @param signature 签名
     * @param timestamp 时间戳
     */
    private void handleRefundNotify(String eventType, String body, String serial, String nonce,
            String signature, String timestamp)
    {
        PaymentConfig config = paymentConfigService.get(PaymentProvider.WECHAT);
        boolean verified = verifier.verify(config, body, serial, nonce, signature, timestamp);
        String plain = verified ? verifier.decrypt(config, resourceOf(body)) : null;

        String refundNo = null;
        String wxRefundId = null;
        String refundStatus = null;
        if (plain != null)
        {
            com.alibaba.fastjson2.JSONObject trade = com.alibaba.fastjson2.JSON.parseObject(plain);
            if (trade != null)
            {
                refundNo = trade.getString("out_refund_no");
                wxRefundId = trade.getString("refund_id");
                refundStatus = trade.getString("refund_status");
            }
        }
        else
        {
            // 未验签或解密失败：不解密报文，从顶层 resource 尽力取单号后走主动查单
            com.alibaba.fastjson2.JSONObject resource = resourceOf(body);
            refundNo = resource == null ? null : resource.getString("out_refund_no");
        }

        boolean success = "SUCCESS".equals(refundStatus) || "REFUND.SUCCESS".equals(eventType);
        if (plain == null)
        {
            // 无法确认回调真实性，以微信侧查询结果为准
            refundService.confirmByQuery(refundNo);
            return;
        }
        refundService.handleNotify(refundNo, wxRefundId, success, null);
    }

    /**
     * 从回调报文中取 resource 节点。
     *
     * @param body 报文
     * @return resource，不存在返回 null
     */
    private com.alibaba.fastjson2.JSONObject resourceOf(String body)
    {
        if (body == null || body.isBlank())
        {
            return null;
        }
        try
        {
            com.alibaba.fastjson2.JSONObject payload = com.alibaba.fastjson2.JSON.parseObject(body);
            return payload == null ? null : payload.getJSONObject("resource");
        }
        catch (Exception exception)
        {
            return null;
        }
    }

    /**
     * 处理微信转账结果回调。
     *
     * <p>与支付回调同样遵循「验签优先、查单兜底」：未通过验签时不信任回调内容，
     * 改为按转账单号主动查询。</p>
     *
     * @param eventType 事件类型，如 MCHTRANSFER.BILL.FINISHED
     * @param body 报文
     * @param serial 平台证书序列号
     * @param nonce 随机串
     * @param signature 签名
     * @param timestamp 时间戳
     */
    private void handleTransferNotify(String eventType, String body, String serial, String nonce,
            String signature, String timestamp)
    {
        PaymentConfig config = paymentConfigService.get(PaymentProvider.WECHAT);
        boolean verified = verifier.verify(config, body, serial, nonce, signature, timestamp);
        String plain = verified ? verifier.decrypt(config, resourceOf(body)) : null;

        String outBillNo = null;
        String transferBillNo = null;
        String state = null;
        if (plain != null)
        {
            com.alibaba.fastjson2.JSONObject bill = com.alibaba.fastjson2.JSON.parseObject(plain);
            if (bill != null)
            {
                outBillNo = bill.getString("out_bill_no");
                transferBillNo = bill.getString("transfer_bill_no");
                state = bill.getString("state");
            }
        }
        else
        {
            log.warn("转账回调无法验签或解密，等待主动同步 eventType={}", eventType);
            return;
        }

        boolean success = "SUCCESS".equals(state);
        withdrawService.handleTransferNotify(outBillNo, transferBillNo, success,
                success ? null : "微信转账状态：" + state);
    }
}
