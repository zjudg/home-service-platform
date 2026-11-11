package com.zjude.system.domain;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjude.common.core.domain.BaseEntity;

/**
 * 自动派单候选记录。
 *
 * <p>每次派单都会记录候选师傅与评分，便于运营追溯「为什么派给了这个人」，
 * 也用于失败订单的重试与人工介入。</p>
 */
public class ZjudgDispatchRecord extends BaseEntity
{
    @Serial private static final long serialVersionUID = 1L;

    /** 候选 */
    public static final String RESULT_CANDIDATE = "0";

    /** 派单成功 */
    public static final String RESULT_SUCCESS = "1";

    /** 被拒绝 */
    public static final String RESULT_REJECTED = "2";

    /** 超时未响应 */
    public static final String RESULT_TIMEOUT = "3";

    private String recordId;
    private String orderId;
    private String orderNo;
    private String masterId;
    private BigDecimal score;
    private String result;
    private String reason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date createTime;
    /** 师傅名称，列表展示用 */
    private String masterName;

    public String getRecordId() { return recordId; }
    public void setRecordId(String value) { recordId = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { orderId = value; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { orderNo = value; }
    public String getMasterId() { return masterId; }
    public void setMasterId(String value) { masterId = value; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal value) { score = value; }
    public String getResult() { return result; }
    public void setResult(String value) { result = value; }
    public String getReason() { return reason; }
    public void setReason(String value) { reason = value; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date value) { createTime = value; }
    public String getMasterName() { return masterName; }
    public void setMasterName(String value) { masterName = value; }
}
