package com.zjude.system.domain.enums;

/** 技能证书商品上下架状态，与 zjudg_certificate_product.status 字典保持一致。 */
public enum ZjudgCertificateProductStatus
{
    ON_SALE("0"), OFF_SALE("1");

    private final String value;
    ZjudgCertificateProductStatus(String value) { this.value = value; }
    public String value() { return value; }
}
