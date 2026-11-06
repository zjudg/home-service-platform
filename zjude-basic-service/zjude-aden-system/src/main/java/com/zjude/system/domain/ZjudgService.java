package com.zjude.system.domain;

import java.io.Serial;
import java.math.BigDecimal;
import com.zjude.common.core.domain.BaseEntity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 家政服务。 */
public class ZjudgService extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    private String serviceId;
    private String serviceName;
    private String serviceIntro;
    private String categoryId;
    /** 发布城市ID，为空表示全国 */
    private String cityId;
    /** 城市名称，列表展示用 */
    private String cityName;
    private String categoryName;
    private String carouselImages;
    private BigDecimal price;
    /** 平台佣金比例，0.2 表示抽取 20% */
    private BigDecimal commissionRate;
    private BigDecimal originalPrice;
    private String unitId;
    private String unitName;
    /** 服务单位的后台计量方式。 */
    private String measureType;
    private Integer sortNum;
    private String status;
    private String detailContent;
    private Integer reserveCount;

    public String getServiceId() { return serviceId; }
    public void setServiceId(String value) { serviceId = value; }
    @NotBlank(message = "服务名称不能为空") @Size(max = 128, message = "服务名称长度不能超过128个字符")
    public String getServiceName() { return serviceName; }
    public void setServiceName(String value) { serviceName = value; }
    @Size(max = 500, message = "服务简介长度不能超过500个字符")
    public String getServiceIntro() { return serviceIntro; }
    public void setServiceIntro(String value) { serviceIntro = value; }
    @NotBlank(message = "服务分类不能为空") public String getCategoryId() { return categoryId; }
    public void setCategoryId(String value) { categoryId = value; }
    public String getCityId() { return cityId; }
    public void setCityId(String value) { cityId = value; }
    public String getCityName() { return cityName; }
    public void setCityName(String value) { cityName = value; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String value) { categoryName = value; }
    public String getCarouselImages() { return carouselImages; }
    public void setCarouselImages(String value) { carouselImages = value; }
    @NotNull(message = "价格不能为空") @DecimalMin(value = "0.00", message = "价格不能小于0")
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal value) { price = value; }
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal value) { commissionRate = value; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal value) { originalPrice = value; }
    @NotBlank(message = "服务单位不能为空") public String getUnitId() { return unitId; }
    public void setUnitId(String value) { unitId = value; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String value) { unitName = value; }
    public String getMeasureType() { return measureType; }
    public void setMeasureType(String value) { measureType = value; }
    @NotNull(message = "排序不能为空") public Integer getSortNum() { return sortNum; }
    public void setSortNum(Integer value) { sortNum = value; }
    @NotBlank(message = "状态不能为空") public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getDetailContent() { return detailContent; }
    public void setDetailContent(String value) { detailContent = value; }
    public Integer getReserveCount() { return reserveCount; }
    public void setReserveCount(Integer value) { reserveCount = value; }
}
