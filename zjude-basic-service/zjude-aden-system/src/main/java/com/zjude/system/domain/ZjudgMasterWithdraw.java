package com.zjude.system.domain;

import java.io.Serial;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjude.common.core.domain.BaseEntity;

/**
 * 师傅提现申请。
 *
 * <p>状态流转：待审核 → 已通过待打款 → 已打款；待审核也可直接驳回。
 * 申请提交时即冻结对应余额，防止同一笔钱被重复申请。</p>
 */
public class ZjudgMasterWithdraw extends BaseEntity
{
    @Serial private static final long serialVersionUID = 1L;

    /** 待审核 */
    public static final String STATUS_PENDING = "0";

    /** 已通过，待打款 */
    public static final String STATUS_APPROVED = "1";

    /** 已打款 */
    public static final String STATUS_PAID = "2";

    /** 已驳回 */
    public static final String STATUS_REJECTED = "3";

    /** 打款失败 */
    public static final String STATUS_FAILED = "4";

    private String withdrawId;
    private String withdrawNo;
    private String masterId;
    /** 提现金额（分） */
    private Long amount;
    /** 手续费（分） */
    private Long fee;
    /** 实际到账（分） */
    private Long actualAmount;
    private String withdrawStatus;
    private String payChannel;
    /** 收款账号，展示时脱敏 */
    private String payAccount;
    private String auditBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date auditTime;
    private String auditRemark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date payTime;
    private String transferNo;
    private String failReason;
    /** 师傅名称，列表展示用 */
    private String masterName;

    public String getWithdrawId() { return withdrawId; }
    public void setWithdrawId(String value) { withdrawId = value; }
    public String getWithdrawNo() { return withdrawNo; }
    public void setWithdrawNo(String value) { withdrawNo = value; }
    public String getMasterId() { return masterId; }
    public void setMasterId(String value) { masterId = value; }
    public Long getAmount() { return amount == null ? 0L : amount; }
    public void setAmount(Long value) { amount = value; }
    public Long getFee() { return fee == null ? 0L : fee; }
    public void setFee(Long value) { fee = value; }
    public Long getActualAmount() { return actualAmount == null ? 0L : actualAmount; }
    public void setActualAmount(Long value) { actualAmount = value; }
    public String getWithdrawStatus() { return withdrawStatus; }
    public void setWithdrawStatus(String value) { withdrawStatus = value; }
    public String getPayChannel() { return payChannel; }
    public void setPayChannel(String value) { payChannel = value; }
    public String getPayAccount() { return payAccount; }
    public void setPayAccount(String value) { payAccount = value; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String value) { auditBy = value; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date value) { auditTime = value; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String value) { auditRemark = value; }
    public Date getPayTime() { return payTime; }
    public void setPayTime(Date value) { payTime = value; }
    public String getTransferNo() { return transferNo; }
    public void setTransferNo(String value) { transferNo = value; }
    public String getFailReason() { return failReason; }
    public void setFailReason(String value) { failReason = value; }
    public String getMasterName() { return masterName; }
    public void setMasterName(String value) { masterName = value; }

    /** 是否处于进行中 */
    public boolean isInProgress()
    {
        return STATUS_PENDING.equals(withdrawStatus) || STATUS_APPROVED.equals(withdrawStatus);
    }
}
