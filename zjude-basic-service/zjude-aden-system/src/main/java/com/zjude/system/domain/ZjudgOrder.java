package com.zjude.system.domain;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjude.common.core.domain.BaseEntity;

/** 家政服务订单查询模型。 */
public class ZjudgOrder extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    private String orderId;
    private String orderNo;
    private String serviceId;
    private String serviceName;
    private String userId;
    private String userInfo;
    @JsonFormat(pattern = "yyyy-MM-dd") private Date appointmentDate;
    private String slotId;
    private String slotName;
    private BigDecimal amount;
    private String orderStatus;
    private String paymentStatus;
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { orderId = value; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { orderNo = value; }
    public String getServiceId() { return serviceId; }
    public void setServiceId(String value) { serviceId = value; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String value) { serviceName = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { userId = value; }
    public String getUserInfo() { return userInfo; }
    public void setUserInfo(String value) { userInfo = value; }
    public Date getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(Date value) { appointmentDate = value; }
    public String getSlotId() { return slotId; }
    public void setSlotId(String value) { slotId = value; }
    public String getSlotName() { return slotName; }
    public void setSlotName(String value) { slotName = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String value) { orderStatus = value; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String value) { paymentStatus = value; }
}
