package com.zjude.system.domain.enums;

/** 技能证书申请状态，与 zjudg_certificate_application.status 字典保持一致。 */
public enum ZjudgCertificateApplicationStatus
{
    PENDING_PAYMENT("0"), PENDING_REVIEW("1"), PROCESSING("2"), SHIPPED("3"), COMPLETED("4"), CANCELLED("5");

    private final String value;
    ZjudgCertificateApplicationStatus(String value) { this.value = value; }
    public String value() { return value; }
}
