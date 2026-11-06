package com.zjude.integrated.payment;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zjude.common.exception.ServiceException;
import com.zjude.integrated.security.PemUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Creates WeChat Pay v3 JSAPI prepay orders and client-side payment signatures. */
@Service
public class WechatPayClient
{
    private static final String JSAPI_PATH = "/v3/pay/transactions/jsapi";
    private static final String APP_PATH = "/v3/pay/transactions/app";
    private static final String QUERY_PATH = "/v3/pay/transactions/out-trade-no";
    private static final String REFUND_PATH = "/v3/refund/domestic/refunds";
    private static final String API_HOST = "https://api.mch.weixin.qq.com";
    private final PaymentConfigService configService;

    public WechatPayClient(PaymentConfigService configService)
    {
        this.configService = configService;
    }

    public Map<String, Object> createJsapiOrder(String description, String outTradeNo,
                                                BigDecimal amount, String openId)
    {
        PaymentConfig config = configService.get(PaymentProvider.WECHAT);
        if (!config.enabled()) throw new ServiceException("微信支付未启用");
        require(config.appId(), "微信支付 AppID");
        require(config.mchId(), "微信支付商户号");
        require(config.apiV3Key(), "微信支付 APIv3 密钥");
        require(config.privateKey(), "微信支付商户私钥");
        require(config.certificateSerialNo(), "微信支付证书序列号");
        require(config.wechatPublicKeyId(), "微信支付公钥 ID");
        require(config.wechatPublicKey(), "微信支付公钥");
        require(config.notifyUrl(), "微信支付回调地址");
        require(openId, "微信用户 OpenID");

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("appid", config.appId());
        requestBody.put("mchid", config.mchId());
        requestBody.put("description", orderDescription(config, description));
        requestBody.put("out_trade_no", outTradeNo);
        requestBody.put("notify_url", config.notifyUrl());
        Map<String, Object> amountBody = new LinkedHashMap<>();
        amountBody.put("total", amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValueExact());
        amountBody.put("currency", "CNY");
        requestBody.put("amount", amountBody);
        requestBody.put("payer", Map.of("openid", openId));

        String body = JSON.toJSONString(requestBody);
        String nonce = UUID.randomUUID().toString().replace("-", "");
        long timestamp = System.currentTimeMillis() / 1000;
        try
        {
            String authorization = authorization(config, nonce, timestamp, body, JSAPI_PATH);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(API_HOST + JSAPI_PATH))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "zjude-super-mall/1.0")
                    .header("Authorization", authorization)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw new ServiceException("微信支付下单失败，微信返回 HTTP " + response.statusCode());
            }
            JSONObject result = JSON.parseObject(response.body());
            String prepayId = result == null ? null : result.getString("prepay_id");
            require(prepayId, "微信支付预支付订单号");
            String payTimestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String payNonce = UUID.randomUUID().toString().replace("-", "");
            String packageValue = "prepay_id=" + prepayId;
            String paySign = sign(config.privateKey(), config.appId() + "\n" + payTimestamp + "\n"
                    + payNonce + "\n" + packageValue + "\n");
            Map<String, Object> payment = new LinkedHashMap<>();
            payment.put("appId", config.appId());
            payment.put("timeStamp", payTimestamp);
            payment.put("nonceStr", payNonce);
            payment.put("package", packageValue);
            payment.put("signType", "RSA");
            payment.put("paySign", paySign);
            payment.put("prepayId", prepayId);
            return payment;
        }
        catch (ServiceException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new ServiceException("微信支付下单失败，请检查商户私钥和网络配置");
        }
    }

    public Map<String, Object> createAppOrder(String description, String outTradeNo, BigDecimal amount)
    {
        PaymentConfig config = configService.get(PaymentProvider.WECHAT);
        if (!config.enabled()) throw new ServiceException("微信支付未启用");
        require(config.appId(), "微信支付 AppID");
        require(config.mchId(), "微信支付商户号");
        require(config.privateKey(), "微信支付商户私钥");
        require(config.certificateSerialNo(), "微信支付证书序列号");
        require(config.wechatPublicKeyId(), "微信支付公钥 ID");
        require(config.wechatPublicKey(), "微信支付公钥");
        require(config.notifyUrl(), "微信支付回调地址");
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("appid", config.appId());
        requestBody.put("mchid", config.mchId());
        requestBody.put("description", orderDescription(config, description));
        requestBody.put("out_trade_no", outTradeNo);
        requestBody.put("notify_url", config.notifyUrl());
        requestBody.put("amount", Map.of("total", amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP).intValueExact(), "currency", "CNY"));
        String body = JSON.toJSONString(requestBody);
        String nonce = UUID.randomUUID().toString().replace("-", "");
        long timestamp = System.currentTimeMillis() / 1000;
        try
        {
            HttpResponse<String> response = post(config, APP_PATH, nonce, timestamp, body);
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw new ServiceException("微信支付下单失败，微信返回 HTTP " + response.statusCode());
            }
            JSONObject result = JSON.parseObject(response.body());
            String prepayId = result == null ? null : result.getString("prepay_id");
            require(prepayId, "微信支付预支付订单号");
            String payNonce = UUID.randomUUID().toString().replace("-", "");
            String payTimestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String paySign = sign(config.privateKey(), config.appId() + "\n" + config.mchId() + "\n"
                    + prepayId + "\n" + payNonce + "\n" + payTimestamp + "\n");
            Map<String, Object> payment = new LinkedHashMap<>();
            payment.put("appid", config.appId());
            payment.put("partnerid", config.mchId());
            payment.put("prepayid", prepayId);
            payment.put("package", "Sign=WXPay");
            payment.put("noncestr", payNonce);
            payment.put("timestamp", payTimestamp);
            payment.put("sign", paySign);
            return payment;
        }
        catch (ServiceException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new ServiceException("微信支付下单失败，请检查商户私钥和网络配置");
        }
    }

    private HttpResponse<String> post(PaymentConfig config, String path, String nonce, long timestamp,
                                      String body) throws Exception
    {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(API_HOST + path))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("User-Agent", "zjude-super-mall/1.0")
                .header("Authorization", authorization(config, nonce, timestamp, body, path))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    /**
     * 申请微信退款。
     *
     * <p>{@code outRefundNo} 必须全局唯一，同一笔退款重复提交会返回原结果，
     * 因此调用方可以直接用退款单号做幂等。</p>
     *
     * @param outTradeNo 原商户订单号
     * @param outRefundNo 商户退款单号
     * @param refundAmount 退款金额
     * @param totalAmount 原订单总金额
     * @param reason 退款原因，可为空
     * @return 微信返回的退款结果，含 refund_id、status 等字段
     */
    public JSONObject createRefund(String outTradeNo, String outRefundNo, BigDecimal refundAmount,
            BigDecimal totalAmount, String reason)
    {
        PaymentConfig config = configService.get(PaymentProvider.WECHAT);
        if (!config.enabled()) throw new ServiceException("微信支付未启用");
        require(config.mchId(), "微信支付商户号");
        require(config.privateKey(), "微信支付商户私钥");
        require(config.certificateSerialNo(), "微信支付证书序列号");
        require(config.wechatPublicKeyId(), "微信支付公钥 ID");
        require(config.wechatPublicKey(), "微信支付公钥");
        require(config.refundNotifyUrl(), "微信退款回调地址");
        require(outTradeNo, "商户订单号");
        require(outRefundNo, "退款单号");

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("out_trade_no", outTradeNo);
        requestBody.put("out_refund_no", outRefundNo);
        if (reason != null && !reason.isBlank())
        {
            requestBody.put("reason", reason.length() > 80 ? reason.substring(0, 80) : reason);
        }
        requestBody.put("notify_url", config.refundNotifyUrl());
        requestBody.put("amount", Map.of(
                "refund", toFen(refundAmount),
                "total", toFen(totalAmount),
                "currency", "CNY"));

        String body = JSON.toJSONString(requestBody);
        String nonce = UUID.randomUUID().toString().replace("-", "");
        long timestamp = System.currentTimeMillis() / 1000;
        try
        {
            HttpResponse<String> response = post(config, REFUND_PATH, nonce, timestamp, body);
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw new ServiceException("微信退款申请失败，微信返回 HTTP " + response.statusCode()
                        + "：" + extractMessage(response.body()));
            }
            JSONObject result = JSON.parseObject(response.body());
            if (result == null || result.getString("refund_id") == null)
            {
                throw new ServiceException("微信退款申请失败：返回缺少退款单号");
            }
            return result;
        }
        catch (ServiceException ex)
        {
            throw ex;
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("微信退款申请被中断");
        }
        catch (Exception ex)
        {
            throw new ServiceException("微信退款申请失败，请检查商户私钥和网络配置");
        }
    }

    /**
     * 按商户退款单号查询退款结果。
     *
     * @param outRefundNo 商户退款单号
     * @return 退款详情，含 status 字段
     */
    public JSONObject queryRefund(String outRefundNo)
    {
        PaymentConfig config = configService.get(PaymentProvider.WECHAT);
        if (!config.enabled()) throw new ServiceException("微信支付未启用");
        require(config.mchId(), "微信支付商户号");
        require(config.privateKey(), "微信支付商户私钥");
        require(config.certificateSerialNo(), "微信支付证书序列号");
        require(config.wechatPublicKeyId(), "微信支付公钥 ID");
        require(outRefundNo, "退款单号");

        String path = REFUND_PATH + "/" + java.net.URLEncoder.encode(outRefundNo, StandardCharsets.UTF_8);
        String nonce = UUID.randomUUID().toString().replace("-", "");
        long timestamp = System.currentTimeMillis() / 1000;
        try
        {
            HttpClient client = HttpClient.newHttpClient();
            String message = "GET\n" + path + "\n" + timestamp + "\n" + nonce + "\n" + "\n";
            String signature = sign(config.privateKey(), message);
            String authorization = "WECHATPAY2-SHA256-RSA2048 mchid=\"" + config.mchId()
                    + "\",nonce_str=\"" + nonce + "\",timestamp=\"" + timestamp
                    + "\",serial_no=\"" + config.certificateSerialNo() + "\",signature=\"" + signature + "\"";
            HttpRequest request = HttpRequest.newBuilder(URI.create(API_HOST + path))
                    .header("Accept", "application/json")
                    .header("User-Agent", "zjude-super-mall/1.0")
                    .header("Authorization", authorization)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw new ServiceException("微信退款查询失败，HTTP " + response.statusCode());
            }
            return JSON.parseObject(response.body());
        }
        catch (ServiceException ex)
        {
            throw ex;
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("微信退款查询被中断");
        }
        catch (Exception ex)
        {
            throw new ServiceException("微信退款查询失败，请检查商户私钥和网络配置");
        }
    }

    /**
     * 金额转分。
     *
     * @param amount 元
     * @return 分
     */
    private int toFen(BigDecimal amount)
    {
        if (amount == null)
        {
            throw new ServiceException("金额不能为空");
        }
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    /**
     * 从微信错误响应中提取 message，避免把整段报文写进异常。
     *
     * @param body 响应体
     * @return 错误描述
     */
    private String extractMessage(String body)
    {
        if (body == null || body.isBlank())
        {
            return "未知错误";
        }
        try
        {
            JSONObject parsed = JSON.parseObject(body);
            String message = parsed == null ? null : parsed.getString("message");
            return message == null ? "未知错误" : message;
        }
        catch (Exception exception)
        {
            return "未知错误";
        }
    }

    /**
     * 按商户订单号查询微信支付订单状态。
     *
     * <p>回调验签不可用时的兜底手段：以微信侧的查询结果为准，
     * 避免伪造回调导致订单被置为已支付。</p>
     *
     * @param outTradeNo 商户订单号
     * @return 查询结果，含 trade_state、transaction_id、amount 等字段
     */
    public JSONObject queryByOutTradeNo(String outTradeNo)
    {
        PaymentConfig config = configService.get(PaymentProvider.WECHAT);
        if (!config.enabled()) throw new ServiceException("微信支付未启用");
        require(config.mchId(), "微信支付商户号");
        require(config.privateKey(), "微信支付商户私钥");
        require(config.certificateSerialNo(), "微信支付证书序列号");
        require(config.wechatPublicKeyId(), "微信支付公钥 ID");
        require(outTradeNo, "商户订单号");

        String path = QUERY_PATH + "?mchid=" + config.mchId() + "&out_trade_no="
                + java.net.URLEncoder.encode(outTradeNo, StandardCharsets.UTF_8);
        String nonce = UUID.randomUUID().toString().replace("-", "");
        long timestamp = System.currentTimeMillis() / 1000;
        try
        {
            HttpClient client = HttpClient.newHttpClient();
            // 查单为 GET 请求，签名串使用 GET 方法与带查询串的路径
            String message = "GET\n" + path + "\n" + timestamp + "\n" + nonce + "\n" + "\n";
            String signature = sign(config.privateKey(), message);
            String authorization = "WECHATPAY2-SHA256-RSA2048 mchid=\"" + config.mchId()
                    + "\",nonce_str=\"" + nonce + "\",timestamp=\"" + timestamp
                    + "\",serial_no=\"" + config.certificateSerialNo() + "\",signature=\"" + signature + "\"";
            HttpRequest request = HttpRequest.newBuilder(URI.create(API_HOST + path))
                    .header("Accept", "application/json")
                    .header("User-Agent", "zjude-super-mall/1.0")
                    .header("Authorization", authorization)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw new ServiceException("微信支付查单失败，HTTP " + response.statusCode());
            }
            return JSON.parseObject(response.body());
        }
        catch (ServiceException ex)
        {
            throw ex;
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("微信支付查单被中断");
        }
        catch (Exception ex)
        {
            throw new ServiceException("微信支付查单失败，请检查商户私钥和网络配置");
        }
    }

    private String authorization(PaymentConfig config, String nonce, long timestamp, String body, String path) throws Exception
    {
        String message = "POST\n" + path + "\n" + timestamp + "\n" + nonce + "\n" + body + "\n";
        String signature = sign(config.privateKey(), message);
        return "WECHATPAY2-SHA256-RSA2048 mchid=\"" + config.mchId() + "\",nonce_str=\""
                + nonce + "\",timestamp=\"" + timestamp + "\",serial_no=\""
                + config.certificateSerialNo() + "\",signature=\"" + signature + "\"";
    }

    private String sign(String privateKeyValue, String content) throws Exception
    {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(readPrivateKey(privateKeyValue));
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    private String orderDescription(PaymentConfig config, String description)
    {
        String value = description == null || description.isBlank() ? "家政服务订单" : description.trim();
        String full = (config.orderDescriptionPrefix() == null ? "" : config.orderDescriptionPrefix().trim()) + value;
        return full.length() <= 127 ? full : full.substring(0, 127);
    }

    /**
     * 读取后台录入的商户私钥 PEM 文本。
     *
     * @param value 私钥内容
     * @return 私钥
     */
    private PrivateKey readPrivateKey(String value)
    {
        return PemUtils.parsePrivateKey(value, "微信支付商户私钥");
    }

    private void require(String value, String name)
    {
        if (value == null || value.isBlank()) throw new ServiceException(name + "不能为空");
    }
}
