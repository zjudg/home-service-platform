package com.zjude.integrated.payment;

/**
 * 供支付业务使用的运行时配置。
 */
public record PaymentConfig(
        PaymentProvider provider,
        String appId,
        String mchId,
        String apiV3Key,
        String certificate,
        String privateKey,
        String certificateSerialNo,
        String gateway,
        String signType,
        String charset,
        String notifyUrl,
        String returnUrl,
        String authDirectory,
        String publicKey,
        String platformCertificate,
        String platformSerialNo,
        String wechatPublicKeyId,
        String wechatPublicKey,
        String refundNotifyUrl,
        String orderDescriptionPrefix,
        boolean enabled)
{
}
