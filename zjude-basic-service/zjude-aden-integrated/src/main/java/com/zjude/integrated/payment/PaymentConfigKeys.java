package com.zjude.integrated.payment;

/**
 * 支付配置在 sys_config 中使用的键名。
 */
public final class PaymentConfigKeys
{
    public static final String WECHAT_ENABLED = "payment.wechat.status";
    public static final String WECHAT_APP_ID = "payment.wechat.appId";
    public static final String WECHAT_MCH_ID = "payment.wechat.mchId";
    public static final String WECHAT_API_V3_KEY = "payment.wechat.apiV3Key";
    public static final String WECHAT_CERTIFICATE = "payment.wechat.certificate";
    public static final String WECHAT_PRIVATE_KEY = "payment.wechat.privateKey";
    public static final String WECHAT_CERTIFICATE_SERIAL_NO = "payment.wechat.certificateSerialNo";
    public static final String WECHAT_NOTIFY_URL = "payment.wechat.notifyUrl";
    public static final String WECHAT_AUTH_DIRECTORY = "payment.wechat.authDirectory";

    /** 微信支付平台证书内容，用于回调验签 */
    public static final String WECHAT_PLATFORM_CERTIFICATE = "payment.wechat.platformCertificate";

    /** 微信支付平台证书序列号 */
    public static final String WECHAT_PLATFORM_SERIAL_NO = "payment.wechat.platformSerialNo";
    public static final String WECHAT_PUBLIC_KEY_ID = "payment.wechat.publicKeyId";
    public static final String WECHAT_PUBLIC_KEY = "payment.wechat.publicKey";
    public static final String WECHAT_REFUND_NOTIFY_URL = "payment.wechat.refundNotifyUrl";
    public static final String WECHAT_ORDER_DESCRIPTION_PREFIX = "payment.wechat.orderDescriptionPrefix";

    public static final String ALIPAY_ENABLED = "payment.alipay.status";
    public static final String ALIPAY_APP_ID = "payment.alipay.appId";
    public static final String ALIPAY_PRIVATE_KEY = "payment.alipay.privateKey";
    public static final String ALIPAY_PUBLIC_KEY = "payment.alipay.publicKey";
    public static final String ALIPAY_GATEWAY = "payment.alipay.gateway";
    public static final String ALIPAY_SIGN_TYPE = "payment.alipay.signType";
    public static final String ALIPAY_CHARSET = "payment.alipay.charset";
    public static final String ALIPAY_NOTIFY_URL = "payment.alipay.notifyUrl";
    public static final String ALIPAY_RETURN_URL = "payment.alipay.returnUrl";

    private PaymentConfigKeys()
    {
    }
}
