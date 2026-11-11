package com.zjude.system.service;

import com.zjude.system.domain.ZjudgMasterWithdraw;

/**
 * 提现打款执行器。
 *
 * <p>把「如何打款」从提现状态机中解耦：服务层只负责余额与状态流转，
 * 具体打款方式（微信自动转账 / 后台手工转账）由实现类决定。</p>
 *
 * <p>这样在未开通商家转账权限时可以退化为手工模式，不需要改动状态机。</p>
 */
public interface IZjudgWithdrawPayer
{
    /**
     * 是否启用自动打款。
     *
     * @return 启用返回 true
     */
    boolean autoEnabled();

    /**
     * 执行打款。
     *
     * <p>实现应保证幂等：同一提现单重复调用不会重复出款。</p>
     *
     * @param withdraw 提现单
     * @return 打款结果
     */
    PayoutResult payout(ZjudgMasterWithdraw withdraw);

    /**
     * 查询打款结果，不发起新的转账。
     *
     * <p>用于回调丢失后的主动补偿，必须实现为纯查询，重复调用不得产生额外出款。</p>
     *
     * @param withdraw 提现单
     * @return 打款结果；无法查询时返回 pending
     */
    PayoutResult query(ZjudgMasterWithdraw withdraw);

    /**
     * 打款结果。
     *
     * @param success 是否已完成打款
     * @param pending 是否仍在处理中（如微信返回 PROCESSING）
     * @param transferNo 打款单号
     * @param reason 失败原因
     */
    record PayoutResult(boolean success, boolean pending, String transferNo, String reason)
    {
        /** 打款成功 */
        public static PayoutResult ok(String transferNo)
        {
            return new PayoutResult(true, false, transferNo, null);
        }

        /** 处理中，等待回调或主动查询 */
        public static PayoutResult processing(String transferNo)
        {
            return new PayoutResult(false, true, transferNo, null);
        }

        /** 打款失败 */
        public static PayoutResult fail(String reason)
        {
            return new PayoutResult(false, false, null, reason);
        }
    }
}
