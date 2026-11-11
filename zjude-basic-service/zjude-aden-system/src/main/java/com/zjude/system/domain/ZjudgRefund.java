package com.zjude.system.domain;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjude.common.core.domain.BaseEntity;

/**
 * 订单退款单。
 *
 * <p>一次退款申请对应一条记录，{@code refundNo} 全局唯一，同时作为微信侧的
 * {@code out_refund_no}，因此重复提交可被微信幂等处理。</p>
 */
public class ZjudgRefund extends BaseEntity
{
    @Serial private static final long serialVersionUID = 1L;

    /** 退款状态：待处理（用户已申请，等待审核） */
    public static final String STATUS_PENDING = "0";

    /** 退款状态：退款中（已提交微信，等待结果） */
    public static final String STATUS_PROCESSING = "1";

    /** 退款状态：已退款 */
    public static final String STATUS_SUCCESS = "2";

    /** 退款状态：退款失败 */
    public static final String STATUS_FAILED = "3";

    /** 退款状态：已拒绝 */
    public static final String STATUS_REJECTED = "4";

    private String refundId;
    /** 商户退款单号，与微信 out_refund_no 一致 */
    private String refundNo;
    private String orderId;
    private String orderNo;
    /** 申请人信息快照 */
    private String buyerInfo;
    private BigDecimal refundAmount;
    /** 退款类型（0 用户申请 1 平台发起） */
    private String refundType;
    private String refundStatus;
    /** 微信退款单号 */
    private String wxRefundId;
    /** 失败或拒绝原因 */
    private String failReason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date refundTime;

    public String getRefundId() { return refundId; }
    public void setRefundId(String value) { refundId = value; }
    public String getRefundNo() { return refundNo; }
    public void setRefundNo(String value) { refundNo = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { orderId = value; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { orderNo = value; }
    public String getBuyerInfo() { return buyerInfo; }
    public void setBuyerInfo(String value) { buyerInfo = value; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal value) { refundAmount = value; }
    public String getRefundType() { return refundType; }
    public void setRefundType(String value) { refundType = value; }
    public String getRefundStatus() { return refundStatus; }
    public void setRefundStatus(String value) { refundStatus = value; }
    public String getWxRefundId() { return wxRefundId; }
    public void setWxRefundId(String value) { wxRefundId = value; }
    public String getFailReason() { return failReason; }
    public void setFailReason(String value) { failReason = value; }
    public Date getRefundTime() { return refundTime; }
    public void setRefundTime(Date value) { refundTime = value; }

    /** 是否处于进行中，用于阻止同一订单重复申请 */
    public boolean isInProgress()
    {
        return STATUS_PENDING.equals(refundStatus) || STATUS_PROCESSING.equals(refundStatus);
    }
}
