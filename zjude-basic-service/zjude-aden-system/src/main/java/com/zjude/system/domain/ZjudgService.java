package com.zjude.system.domain;

import java.io.Serial;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonInclude;
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
    /** 服务详情页主图。 */
    private String detailImage;
    /** 商品列表展示图。 */
    private String productImage;
    private BigDecimal price;
    /** 师傅每个服务计量单位的到手价，平台利润=卖价-接单价。 */
    private BigDecimal masterPrice;
    /** 平台扣款方式：FIXED 固定金额，RATE 按卖价百分比。 */
    private String commissionType;
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
    /** 1 表示仅用于用户原订单的补时支付，不参加普通服务目录展示。 */
    private Integer extraTimeOnly;

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
    @NotBlank(message = "服务详情主图不能为空")
    public String getDetailImage() { return detailImage; }
    public void setDetailImage(String value) { detailImage = value; }
    @NotBlank(message = "商品列表图不能为空")
    public String getProductImage() { return productImage; }
    public void setProductImage(String value) { productImage = value; }
    @NotNull(message = "价格不能为空") @DecimalMin(value = "0.00", message = "价格不能小于0")
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal value) { price = value; }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @NotNull(message = "接单价不能为空") @DecimalMin(value = "0.00", message = "接单价不能小于0")
    public BigDecimal getMasterPrice() { return masterPrice; }
    public void setMasterPrice(BigDecimal value) { masterPrice = value; }
    public String getCommissionType() { return commissionType; }
    public void setCommissionType(String value) { commissionType = value; }
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
    public Integer getExtraTimeOnly() { return extraTimeOnly; }
    public void setExtraTimeOnly(Integer value) { extraTimeOnly = value; }
}
