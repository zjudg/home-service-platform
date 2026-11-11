package com.zjude.system.domain;

import java.io.Serial;
import java.math.BigDecimal;
import com.zjude.common.core.domain.BaseEntity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 可在线购买的技能证书商品；不包含身份证采集字段。 */
public class ZjudgCertificateProduct extends BaseEntity
{
    @Serial private static final long serialVersionUID = 1L;
    private String productId;
    private String productName;
    private String productIntro;
    private BigDecimal price;
    private String coverUrl;
    private String status;
    private Integer sortNum;

    public String getProductId() { return productId; }
    public void setProductId(String value) { productId = value; }
    @NotBlank(message = "证书名称不能为空") public String getProductName() { return productName; }
    public void setProductName(String value) { productName = value; }
    public String getProductIntro() { return productIntro; }
    public void setProductIntro(String value) { productIntro = value; }
    @NotNull(message = "证书价格不能为空") @DecimalMin(value = "0.00", message = "证书价格不能小于0")
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal value) { price = value; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String value) { coverUrl = value; }
    @NotBlank(message = "证书状态不能为空") public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    @NotNull(message = "排序不能为空") public Integer getSortNum() { return sortNum; }
    public void setSortNum(Integer value) { sortNum = value; }
}
