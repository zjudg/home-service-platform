package com.zjude.integrated.payment;

public interface PaymentConfigService
{
    /**
     * 读取完整支付配置，仅供服务端支付 SDK 使用。
     */
    PaymentConfig get(PaymentProvider provider);

    /**
     * 校验启用中的支付配置是否完整。
     */
    void validate(PaymentProvider provider);
}
