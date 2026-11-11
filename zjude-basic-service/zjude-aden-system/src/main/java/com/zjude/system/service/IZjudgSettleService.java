package com.zjude.system.service;

import java.util.List;
import com.zjude.system.domain.ZjudgMasterWallet;
import com.zjude.system.domain.ZjudgMasterWalletLog;

/**
 * 师傅结算服务。
 *
 * <p>结算口径：订单进入「已完成」且已支付时，按服务配置的佣金比例计算师傅收入并即时入账。
 * 订单被退款或取消时冲回已入账收入。</p>
 *
 * <p>所有金额以「分」为单位的 long 传递，避免浮点误差。</p>
 */
public interface IZjudgSettleService
{
    /**
     * 订单完工结算：计算师傅收入并写入钱包。
     *
     * <p>幂等：同一订单只会结算一次，重复调用不会重复入账。</p>
     *
     * @param orderId 订单主键
     * @return 本次入账金额（分），0 表示无需结算或已结算
     */
    long settleOrder(String orderId);

    /**
     * 冲回订单已结算的收入，用于退款或取消。
     *
     * @param orderId 订单主键
     * @param reason 冲回原因
     * @return 冲回金额（分），0 表示未结算过
     */
    long reverseOrder(String orderId, String reason);

    /**
     * 查询师傅钱包，不存在时自动创建。
     *
     * @param masterId 师傅主键
     * @return 钱包
     */
    ZjudgMasterWallet getWallet(String masterId);

    /**
     * 查询师傅钱包流水。
     *
     * @param masterId 师傅主键
     * @param bizType 可选业务类型
     * @return 流水列表
     */
    List<ZjudgMasterWalletLog> listLogs(String masterId, String bizType);

    /**
     * 后台查询全部流水。
     *
     * @param query 查询条件
     * @return 流水列表
     */
    List<ZjudgMasterWalletLog> listAllLogs(ZjudgMasterWalletLog query);
}
