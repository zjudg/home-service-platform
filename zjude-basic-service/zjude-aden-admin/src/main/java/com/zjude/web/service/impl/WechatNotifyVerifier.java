package com.zjude.web.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSONObject;
import com.zjude.common.utils.StringUtils;
import com.zjude.integrated.payment.PaymentConfig;
import com.zjude.integrated.security.PemUtils;

/**
 * 微信支付回调的验签与解密。
 *
 * <p>支付回调与退款回调共用同一套签名与 AES-GCM 加密规则，
 * 因此把这段能力抽出，避免两处各写一份导致行为不一致。</p>
 *
 * <p>验签不可用时返回 false 而不是抛异常，由调用方决定降级策略
 * （本项目采用「验签失败就主动查单」的兜底）。</p>
 */
@Component
public class WechatNotifyVerifier
{
    private static final Logger log = LoggerFactory.getLogger(WechatNotifyVerifier.class);

    /**
     * 校验微信回调签名。
     *
     * @param config 支付配置
     * @param body 报文原文
     * @param nonce 随机串头
     * @param signature 签名头
     * @param timestamp 时间戳头
     * @return 验签通过返回 true
     */
    public boolean verify(PaymentConfig config, String body, String nonce, String signature, String timestamp)
    {
        return verify(config, body, null, nonce, signature, timestamp);
    }

    /**
     * 按微信回调头中的序列号选择验签公钥。公钥模式使用 PUB_KEY_ID_ 开头的 ID，
     * 旧平台证书模式则使用平台证书序列号；两种模式都拒绝序列号不匹配的报文。
     */
    public boolean verify(PaymentConfig config, String body, String serial, String nonce,
            String signature, String timestamp)
    {
        if (config == null || StringUtils.isEmpty(signature) || StringUtils.isEmpty(nonce)
                || StringUtils.isEmpty(timestamp) || StringUtils.isEmpty(serial))
        {
            return false;
        }
        try
        {
            String message = timestamp + "\n" + nonce + "\n" + body + "\n";
            PublicKey publicKey;
            if (serial.startsWith("PUB_KEY_ID_"))
            {
                if (!serial.equals(config.wechatPublicKeyId()) || StringUtils.isEmpty(config.wechatPublicKey()))
                {
                    return false;
                }
                publicKey = PemUtils.parsePublicKey(config.wechatPublicKey(), "微信支付公钥");
            }
            else
            {
                if (StringUtils.isEmpty(config.platformCertificate())
                        || (StringUtils.isNotEmpty(config.platformSerialNo())
                            && !serial.equals(config.platformSerialNo())))
                {
                    return false;
                }
                publicKey = PemUtils.parseCertificatePublicKey(config.platformCertificate(), "微信支付平台证书");
            }
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(message.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(java.util.Base64.getDecoder().decode(signature));
        }
        catch (Exception exception)
        {
            log.warn("微信回调验签异常，将改用主动查询", exception);
            return false;
        }
    }

    /**
     * 使用 APIv3 密钥解密回调 resource。
     *
     * @param config 支付配置
     * @param resource 回调报文的 resource 节点
     * @return 解密后的明文，失败返回 null
     */
    public String decrypt(PaymentConfig config, JSONObject resource)
    {
        if (config == null || StringUtils.isEmpty(config.apiV3Key()) || resource == null)
        {
            return null;
        }
        try
        {
            String ciphertext = resource.getString("ciphertext");
            String nonce = resource.getString("nonce");
            String associatedData = resource.getString("associated_data");
            if (StringUtils.isEmpty(ciphertext) || StringUtils.isEmpty(nonce))
            {
                return null;
            }
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(config.apiV3Key().getBytes(StandardCharsets.UTF_8), "AES");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
            if (StringUtils.isNotEmpty(associatedData))
            {
                cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
            }
            byte[] plain = cipher.doFinal(java.util.Base64.getDecoder().decode(ciphertext));
            return new String(plain, StandardCharsets.UTF_8);
        }
        catch (Exception exception)
        {
            log.warn("微信回调解密失败，将改用主动查询", exception);
            return null;
        }
    }
}
