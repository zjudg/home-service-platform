package com.zjude.system.domain;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjude.common.core.domain.BaseEntity;

/** 关联原订单的补时支付快照。 */
public class ZjudgOrderExtraTime extends BaseEntity
{
    @Serial private static final long serialVersionUID = 1L;
    private String extraTimeId;
    private String extraTimeNo;
    private String orderId;
    private String userId;
    private String masterId;
    private String serviceId;
    private String serviceName;
    private Integer hours;
    private BigDecimal amount;
    private BigDecimal masterAmount;
    private String paymentStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date paidTime;

    public String getExtraTimeId() { return extraTimeId; }
    public void setExtraTimeId(String value) { extraTimeId = value; }
    public String getExtraTimeNo() { return extraTimeNo; }
    public void setExtraTimeNo(String value) { extraTimeNo = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { orderId = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { userId = value; }
    public String getMasterId() { return masterId; }
    public void setMasterId(String value) { masterId = value; }
    public String getServiceId() { return serviceId; }
    public void setServiceId(String value) { serviceId = value; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String value) { serviceName = value; }
    public Integer getHours() { return hours; }
    public void setHours(Integer value) { hours = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public BigDecimal getMasterAmount() { return masterAmount; }
    public void setMasterAmount(BigDecimal value) { masterAmount = value; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String value) { paymentStatus = value; }
    public Date getPaidTime() { return paidTime; }
    public void setPaidTime(Date value) { paidTime = value; }
}
