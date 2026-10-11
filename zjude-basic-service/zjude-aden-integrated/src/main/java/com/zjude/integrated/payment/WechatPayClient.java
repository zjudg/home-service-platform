package com.zjude.integrated.payment;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zjude.common.exception.ServiceException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.nio.file.InvalidPathException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import org.springframework.stereotype.Service;

/** Creates WeChat Pay v3 JSAPI prepay orders and client-side payment signatures. */
@Service
public class WechatPayClient
{
    private static final String JSAPI_PATH = "/v3/pay/transactions/jsapi";
    private static final String APP_PATH = "/v3/pay/transactions/app";
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
        require(config.certificate(), "微信支付商户证书");
        require(config.privateKey(), "微信支付商户私钥");
        require(config.certificateSerialNo(), "微信支付证书序列号");
        require(config.notifyUrl(), "微信支付回调地址");
        require(openId, "微信用户 OpenID");

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("appid", config.appId());
        requestBody.put("mchid", config.mchId());
        requestBody.put("description", description == null || description.isBlank() ? "商城订单" : description);
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
            HttpClient client = HttpClient.newBuilder()
                    .sslContext(sslContext(config))
                    .build();
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
            throw new ServiceException("微信支付下单失败，请检查商户证书和网络配置");
        }
    }

    public Map<String, Object> createAppOrder(String description, String outTradeNo, BigDecimal amount)
    {
        PaymentConfig config = configService.get(PaymentProvider.WECHAT);
        if (!config.enabled()) throw new ServiceException("微信支付未启用");
        require(config.appId(), "微信支付 AppID");
        require(config.mchId(), "微信支付商户号");
        require(config.privateKey(), "微信支付商户私钥");
        require(config.certificate(), "微信支付商户证书");
        require(config.certificateSerialNo(), "微信支付证书序列号");
        require(config.notifyUrl(), "微信支付回调地址");
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("appid", config.appId());
        requestBody.put("mchid", config.mchId());
        requestBody.put("description", description == null || description.isBlank() ? "商城订单" : description);
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
            throw new ServiceException("微信支付下单失败，请检查商户证书和网络配置");
        }
    }

    private HttpResponse<String> post(PaymentConfig config, String path, String nonce, long timestamp,
                                      String body) throws Exception
    {
        HttpClient client = HttpClient.newBuilder().sslContext(sslContext(config)).build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(API_HOST + path))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("User-Agent", "zjude-super-mall/1.0")
                .header("Authorization", authorization(config, nonce, timestamp, body, path))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
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

    private SSLContext sslContext(PaymentConfig config) throws Exception
    {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        Certificate certificate = factory.generateCertificate(new java.io.ByteArrayInputStream(
                resolvePem(config.certificate()).getBytes(StandardCharsets.UTF_8)));
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry("wechat-pay", readPrivateKey(config.privateKey()), new char[0], new Certificate[] { certificate });
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, new char[0]);
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
        return context;
    }

    private PrivateKey readPrivateKey(String value) throws Exception
    {
        String pem = resolvePem(value).replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");
        return KeyFactory.getInstance("RSA").generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(Base64.getDecoder().decode(pem)));
    }

    private String resolvePem(String value) throws Exception
    {
        try
        {
            Path path = Paths.get(value);
            return Files.isRegularFile(path) ? Files.readString(path, StandardCharsets.UTF_8) : value;
        }
        catch (InvalidPathException ex)
        {
            return value;
        }
    }

    private void require(String value, String name)
    {
        if (value == null || value.isBlank()) throw new ServiceException(name + "不能为空");
    }
}
