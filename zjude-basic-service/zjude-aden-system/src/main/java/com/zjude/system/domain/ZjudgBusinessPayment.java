package com.zjude.system.domain;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjude.common.core.domain.BaseEntity;

public class ZjudgBusinessPayment extends BaseEntity
{
    @Serial private static final long serialVersionUID = 1L;
    private String paymentId;
    private String bizType;
    private String bizId;
    private String userId;
    private String outTradeNo;
    private String transactionId;
    private BigDecimal amount;
    private String payStatus;
    private String prepayId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date paidTime;
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String value) { paymentId = value; }
    public String getBizType() { return bizType; }
    public void setBizType(String value) { bizType = value; }
    public String getBizId() { return bizId; }
    public void setBizId(String value) { bizId = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { userId = value; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String value) { outTradeNo = value; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String value) { transactionId = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public String getPayStatus() { return payStatus; }
    public void setPayStatus(String value) { payStatus = value; }
    public String getPrepayId() { return prepayId; }
    public void setPrepayId(String value) { prepayId = value; }
    public Date getPaidTime() { return paidTime; }
    public void setPaidTime(Date value) { paidTime = value; }
}
