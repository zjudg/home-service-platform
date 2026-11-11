package com.zjude.integrated.payment;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import javax.net.ssl.SSLContext;
import org.springframework.stereotype.Component;
import com.zjude.common.exception.ServiceException;
import com.zjude.integrated.security.PemUtils;

/**
 * 微信支付 v3 请求签名。
 *
 * <p>支付、退款、转账、查单共用同一套签名规则，抽出以避免多处实现漂移。
 * 注意 GET 与 POST 的签名串不同：POST 需要把请求体纳入签名。</p>
 */
@Component
public class WechatPaySigner
{
    private static final String SCHEMA = "WECHATPAY2-SHA256-RSA2048";

    /**
     * 构建 POST 请求的 Authorization 头。
     *
     * @param config 支付配置
     * @param method HTTP 方法
     * @param path 请求路径（含查询串）
     * @param body 请求体，GET 时传空串
     * @param nonce 随机串
     * @param timestamp 秒级时间戳
     * @return Authorization 头
     * @throws Exception 签名失败
     */
    public String authorization(PaymentConfig config, String method, String path, String body, String nonce,
            long timestamp) throws Exception
    {
        // 签名串：方法\n路径\n时间戳\n随机串\n请求体\n（GET 时请求体为空）
        String message = method + "\n" + path + "\n" + timestamp + "\n" + nonce + "\n"
                + (body == null ? "" : body) + "\n";
        String signature = sign(config.privateKey(), message);
        return SCHEMA + " mchid=\"" + config.mchId()
                + "\",nonce_str=\"" + nonce
                + "\",timestamp=\"" + timestamp
                + "\",serial_no=\"" + config.certificateSerialNo()
                + "\",signature=\"" + signature + "\"";
    }

    /**
     * 使用商户私钥签名。
     *
     * @param privateKeyValue 私钥 PEM 内容
     * @param content 待签名内容
     * @return Base64 签名
     * @throws Exception 签名失败
     */
    public String sign(String privateKeyValue, String content) throws Exception
    {
        PrivateKey privateKey = PemUtils.parsePrivateKey(privateKeyValue, "微信支付商户私钥");
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    /**
     * 返回默认 TLS 上下文。微信支付公钥模式不要求商户证书双向 TLS，
     * 请求身份由 Authorization 中的商户私钥签名确认。
     *
     * @param config 支付配置
     * @return SSLContext
     * @throws Exception 获取 TLS 上下文失败
     */
    public SSLContext sslContext(PaymentConfig config) throws Exception
    {
        return SSLContext.getDefault();
    }

    /**
     * 校验必需的配置项。
     *
     * @param config 支付配置
     */
    public void requireReady(PaymentConfig config)
    {
        if (config == null || !config.enabled())
        {
            throw new ServiceException("微信支付未启用");
        }
        if (config.appId() == null || config.appId().isBlank())
        {
            throw new ServiceException("小程序 AppID 未配置");
        }
        if (config.mchId() == null || config.mchId().isBlank())
        {
            throw new ServiceException("微信支付商户号未配置");
        }
        if (config.privateKey() == null || config.privateKey().isBlank())
        {
            throw new ServiceException("微信支付商户私钥未配置");
        }
        if (config.certificateSerialNo() == null || config.certificateSerialNo().isBlank())
        {
            throw new ServiceException("微信支付证书序列号未配置");
        }
    }
}
