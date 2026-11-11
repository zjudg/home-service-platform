package com.zjude.common.enums;

/** 家政订单收款渠道。 */
public enum ZjudgOrderPaymentSource
{
    /** 小程序线上微信收款。 */
    WECHAT("WECHAT", "微信支付"),
    /** 平台外线下收款。 */
    OFFLINE("OFFLINE", "线下收款");

    private final String code;
    private final String label;

    ZjudgOrderPaymentSource(String code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }
}
