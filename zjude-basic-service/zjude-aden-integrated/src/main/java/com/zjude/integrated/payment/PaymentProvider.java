package com.zjude.integrated.payment;

public enum PaymentProvider
{
    WECHAT("wechat"),
    ALIPAY("alipay");

    private final String code;

    PaymentProvider(String code)
    {
        this.code = code;
    }

    public String getCode()
    {
        return code;
    }
}
