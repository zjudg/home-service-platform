package com.zjude.system.domain.enums;

/** 补时账单支付状态，和 zjudg_order_extra_time.payment_status 字典一致。 */
public enum ZjudgExtraTimePaymentStatus
{
    PENDING("0", "待支付"),
    PAID("1", "已支付");

    private final String value;
    private final String label;

    ZjudgExtraTimePaymentStatus(String value, String label)
    {
        this.value = value;
        this.label = label;
    }

    public String value() { return value; }
    public String label() { return label; }
}
