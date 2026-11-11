package com.zjude.system.domain;

import java.io.Serial;
import com.zjude.common.core.domain.BaseEntity;

/**
 * 师傅钱包。
 *
 * <p>所有金额均以「分」为单位的 long 存储，避免浮点误差。
 * 可用余额 = balance，提现申请时从 balance 划入 frozen，打款成功后从 frozen 扣除。</p>
 */
public class ZjudgMasterWallet extends BaseEntity
{
    @Serial private static final long serialVersionUID = 1L;

    private String walletId;
    private String masterId;
    /** 可用余额（分） */
    private Long balance;
    /** 提现冻结金额（分） */
    private Long frozen;
    /** 累计收入（分） */
    private Long totalIncome;
    /** 累计提现（分） */
    private Long totalWithdraw;
    /** 乐观锁版本号 */
    private Integer version;

    public String getWalletId() { return walletId; }
    public void setWalletId(String value) { walletId = value; }
    public String getMasterId() { return masterId; }
    public void setMasterId(String value) { masterId = value; }
    public Long getBalance() { return balance == null ? 0L : balance; }
    public void setBalance(Long value) { balance = value; }
    public Long getFrozen() { return frozen == null ? 0L : frozen; }
    public void setFrozen(Long value) { frozen = value; }
    public Long getTotalIncome() { return totalIncome == null ? 0L : totalIncome; }
    public void setTotalIncome(Long value) { totalIncome = value; }
    public Long getTotalWithdraw() { return totalWithdraw == null ? 0L : totalWithdraw; }
    public void setTotalWithdraw(Long value) { totalWithdraw = value; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer value) { version = value; }
}
