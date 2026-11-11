package com.zjude.system.domain.enums;

/** 非订单支付流水状态，与 zjudg_business_payment.pay_status 字典保持一致。 */
public enum ZjudgBusinessPaymentStatus
{
    PENDING("0"), PAID("1"), CLOSED("2");

    private final String value;
    ZjudgBusinessPaymentStatus(String value) { this.value = value; }
    public String value() { return value; }
}
