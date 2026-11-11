package com.zjude.system.domain;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjude.common.core.domain.BaseEntity;

/** 技能证书申请；线上不采集身份证或身份证照片，但需要办理用电子照片。 */
public class ZjudgCertificateApplication extends BaseEntity
{
    @Serial private static final long serialVersionUID = 1L;
    private String applicationId;
    private String applicationNo;
    private String productId;
    private String productName;
    private String userId;
    private String contactPhone;
    private String photoUrl;
    private String deliveryAddress;
    private BigDecimal amount;
    private String status;
    private String trackingNo;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date createTime;
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String value) { applicationId = value; }
    public String getApplicationNo() { return applicationNo; }
    public void setApplicationNo(String value) { applicationNo = value; }
    public String getProductId() { return productId; }
    public void setProductId(String value) { productId = value; }
    public String getProductName() { return productName; }
    public void setProductName(String value) { productName = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { userId = value; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String value) { contactPhone = value; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String value) { photoUrl = value; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String value) { deliveryAddress = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String value) { trackingNo = value; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date value) { createTime = value; }
}
