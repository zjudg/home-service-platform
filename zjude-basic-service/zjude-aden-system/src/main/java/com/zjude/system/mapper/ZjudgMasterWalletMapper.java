package com.zjude.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zjude.system.domain.ZjudgMasterWallet;
import com.zjude.system.domain.ZjudgMasterWalletLog;

/**
 * 师傅钱包与流水数据访问。
 *
 * <p>所有余额变动都通过带条件的 UPDATE 完成，条件里带上「足够余额」或
 * 「余额未变」的限制，避免并发下超扣或重复入账。</p>
 */
public interface ZjudgMasterWalletMapper
{
    /**
     * 按师傅查询钱包。
     *
     * @param masterId 师傅主键
     * @return 钱包，不存在返回 null
     */
    ZjudgMasterWallet selectByMasterId(@Param("masterId") String masterId);

    /**
     * 按主键查询钱包，加行锁。
     *
     * @param masterId 师傅主键
     * @return 钱包
     */
    ZjudgMasterWallet selectByMasterIdForUpdate(@Param("masterId") String masterId);

    /**
     * 新增钱包。
     *
     * @param wallet 钱包
     * @return 影响行数
     */
    int insert(ZjudgMasterWallet wallet);

    /**
     * 增加收入：余额与累计收入同时增加。
     *
     * @param masterId 师傅主键
     * @param amount 金额（分）
     * @return 影响行数
     */
    int increaseIncome(@Param("masterId") String masterId, @Param("amount") long amount);

    /**
     * 冲回收入：余额减少，累计收入同步扣减。
     *
     * <p>条件限制余额足够，避免扣成负数。</p>
     *
     * @param masterId 师傅主键
     * @param amount 金额（分）
     * @return 影响行数
     */
    int decreaseIncome(@Param("masterId") String masterId, @Param("amount") long amount);

    /**
     * 提现冻结：可用余额转入冻结。
     *
     * @param masterId 师傅主键
     * @param amount 金额（分）
     * @return 影响行数，0 表示余额不足
     */
    int freezeForWithdraw(@Param("masterId") String masterId, @Param("amount") long amount);

    /**
     * 提现成功：扣减冻结与累计提现。
     *
     * @param masterId 师傅主键
     * @param amount 金额（分）
     * @return 影响行数
     */
    int confirmWithdraw(@Param("masterId") String masterId, @Param("amount") long amount);

    /**
     * 提现驳回：冻结退回可用余额。
     *
     * @param masterId 师傅主键
     * @param amount 金额（分）
     * @return 影响行数
     */
    int refundWithdraw(@Param("masterId") String masterId, @Param("amount") long amount);

    /**
     * 写入流水。
     *
     * @param log 流水
     * @return 影响行数
     */
    int insertLog(ZjudgMasterWalletLog log);

    /**
     * 查询流水。
     *
     * @param masterId 师傅主键
     * @param bizType 可选业务类型过滤
     * @return 流水列表
     */
    List<ZjudgMasterWalletLog> selectLogs(@Param("masterId") String masterId,
            @Param("bizType") String bizType);

    /**
     * 后台查询全部流水。
     *
     * @param query 查询条件
     * @return 流水列表
     */
    List<ZjudgMasterWalletLog> selectLogList(ZjudgMasterWalletLog query);
}
