package com.zjude.system.domain;

import java.io.Serial;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjude.common.core.domain.BaseEntity;

/**
 * 师傅钱包流水。
 *
 * <p>每次余额变动必须留一条流水，{@code bizType + bizId} 唯一，
 * 保证同一业务（如同一笔订单）不会被重复记账。</p>
 */
public class ZjudgMasterWalletLog extends BaseEntity
{
    @Serial private static final long serialVersionUID = 1L;

    /** 业务类型：订单收入 */
    public static final String TYPE_ORDER_INCOME = "order_income";

    /** 业务类型：订单补时收入。 */
    public static final String TYPE_EXTRA_TIME_INCOME = "extra_time_income";

    /** 业务类型：订单冲回（退款或取消） */
    public static final String TYPE_ORDER_REVERSE = "order_reverse";

    /** 业务类型：提现冻结转出 */
    public static final String TYPE_WITHDRAW = "withdraw";

    /** 业务类型：提现驳回退回 */
    public static final String TYPE_WITHDRAW_REFUND = "withdraw_refund";

    private String logId;
    private String masterId;
    private String walletId;
    private String bizType;
    /** 变动金额（分），正为收入负为支出 */
    private Long amount;
    /** 变动后可用余额（分） */
    private Long balanceAfter;
    private String bizId;
    private String bizNo;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date createTime;
    /** 师傅名称，后台列表展示用 */
    private String masterName;

    public String getLogId() { return logId; }
    public void setLogId(String value) { logId = value; }
    public String getMasterId() { return masterId; }
    public void setMasterId(String value) { masterId = value; }
    public String getWalletId() { return walletId; }
    public void setWalletId(String value) { walletId = value; }
    public String getBizType() { return bizType; }
    public void setBizType(String value) { bizType = value; }
    public Long getAmount() { return amount == null ? 0L : amount; }
    public void setAmount(Long value) { amount = value; }
    public Long getBalanceAfter() { return balanceAfter == null ? 0L : balanceAfter; }
    public void setBalanceAfter(Long value) { balanceAfter = value; }
    public String getBizId() { return bizId; }
    public void setBizId(String value) { bizId = value; }
    public String getBizNo() { return bizNo; }
    public void setBizNo(String value) { bizNo = value; }
    public String getRemark() { return remark; }
    public void setRemark(String value) { remark = value; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date value) { createTime = value; }
    public String getMasterName() { return masterName; }
    public void setMasterName(String value) { masterName = value; }
}
