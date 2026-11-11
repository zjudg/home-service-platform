package com.zjude.system.service;

import java.util.List;
import com.zjude.system.domain.ZjudgMasterWithdraw;

/**
 * 师傅提现服务。
 *
 * <p>流程：师傅申请（冻结余额）→ 平台审核 → 打款（扣减冻结）。
 * 驳回时冻结金额退回可用余额。</p>
 *
 * <p>资金安全不变量：</p>
 * <ul>
 *   <li>同一师傅同时只能有一条进行中的提现申请；</li>
 *   <li>申请时即冻结，避免同一笔钱被重复申请；</li>
 *   <li>状态流转使用「仅当状态匹配时更新」，重复审核不产生副作用。</li>
 * </ul>
 */
public interface IZjudgMasterWithdrawService
{
    /**
     * 师傅提交提现申请。
     *
     * @param masterId 师傅主键
     * @param amountFen 提现金额（分）
     * @param payAccount 收款账号
     * @return 提现单
     */
    ZjudgMasterWithdraw apply(String masterId, long amountFen, String payAccount);

    /**
     * 师傅查询自己的提现记录。
     *
     * @param masterId 师傅主键
     * @return 提现列表
     */
    List<ZjudgMasterWithdraw> listByMaster(String masterId);

    /**
     * 后台查询提现列表。
     *
     * @param query 查询条件
     * @return 提现列表
     */
    List<ZjudgMasterWithdraw> selectList(ZjudgMasterWithdraw query);

    /**
     * 审核通过，进入待打款。
     *
     * @param withdrawId 提现主键
     * @param operator 审核人
     * @param remark 审核意见
     * @return 影响行数
     */
    int approve(String withdrawId, String operator, String remark);

    /**
     * 驳回提现申请，冻结金额退回。
     *
     * @param withdrawId 提现主键
     * @param operator 审核人
     * @param reason 驳回原因
     * @return 影响行数
     */
    int reject(String withdrawId, String operator, String reason);

    /**
     * 标记打款完成，扣减冻结金额。
     *
     * @param withdrawId 提现主键
     * @param transferNo 打款单号
     * @param operator 操作人
     * @return 影响行数
     */
    int markPaid(String withdrawId, String transferNo, String operator);

    /**
     * 标记打款失败，冻结金额退回。
     *
     * @param withdrawId 提现主键
     * @param reason 失败原因
     * @param operator 操作人
     * @return 影响行数
     */
    int markFailed(String withdrawId, String reason, String operator);

    /**
     * 处理微信转账结果回调。
     *
     * <p>按商户转账单号定位提现单，成功则扣减冻结，失败则退回。</p>
     *
     * @param outBillNo 商户转账单号（等于提现单号）
     * @param transferBillNo 微信转账单号
     * @param success 是否转账成功
     * @param reason 失败原因
     * @return 是否处理成功
     */
    boolean handleTransferNotify(String outBillNo, String transferBillNo, boolean success, String reason);

    /**
     * 按转账单号主动查询并同步打款结果。
     *
     * @param withdrawId 提现主键
     * @return 同步后的提现单
     */
    ZjudgMasterWithdraw syncTransfer(String withdrawId);
}
