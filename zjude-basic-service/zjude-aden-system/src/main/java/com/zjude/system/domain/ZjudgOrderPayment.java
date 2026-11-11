package com.zjude.system.domain;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjude.common.core.domain.BaseEntity;

/**
 * 订单支付流水。
 *
 * <p>每一次支付尝试都会落一条流水，用于对账与问题排查。{@code outTradeNo}
 * 是传给微信的商户单号，唯一约束保证不会重复下单。</p>
 */
public class ZjudgOrderPayment extends BaseEntity
{
    @Serial private static final long serialVersionUID = 1L;

    /** 支付状态：待支付 */
    public static final String STATUS_PENDING = "0";

    /** 支付状态：已支付 */
    public static final String STATUS_PAID = "1";

    /** 支付状态：已关闭 */
    public static final String STATUS_CLOSED = "2";

    /** 支付状态：已退款 */
    public static final String STATUS_REFUNDED = "3";

    private String paymentId;
    private String orderId;
    private String orderNo;
    private String provider;
    /** 商户订单号，传给微信的唯一单号 */
    private String outTradeNo;
    /** 微信支付交易号 */
    private String transactionId;
    private BigDecimal amount;
    private String payStatus;
    private String prepayId;
    /** 回调原始报文，脱敏后保存，便于排查 */
    private String notifyRaw;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date paidTime;

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String value) { paymentId = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { orderId = value; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { orderNo = value; }
    public String getProvider() { return provider; }
    public void setProvider(String value) { provider = value; }
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
    public String getNotifyRaw() { return notifyRaw; }
    public void setNotifyRaw(String value) { notifyRaw = value; }
    public Date getPaidTime() { return paidTime; }
    public void setPaidTime(Date value) { paidTime = value; }
}
