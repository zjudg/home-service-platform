package com.zjude.system.service;

import java.util.List;
import com.zjude.system.domain.ZjudgDispatchRecord;
import com.zjude.system.domain.ZjudgOrder;

/**
 * 自动派单服务。
 *
 * <p>派单策略为「评分排序 + 先到先得」：</p>
 * <ol>
 *   <li>筛选硬性条件：审核通过、账号正常、接单开关打开、未被停用；</li>
 *   <li>按技能匹配、城市匹配、评分、当前负载加权打分；</li>
 *   <li>取分数最高的一位，用带条件的 UPDATE 抢占，失败则顺延下一位；</li>
 *   <li>全过程写入派单记录，便于追溯。</li>
 * </ol>
 *
 * <p>不依赖外部调度器：由业务事件（支付成功）或后台手动触发。</p>
 */
public interface IZjudgDispatchService
{
    /**
     * 为订单自动派单。
     *
     * @param orderId 订单主键
     * @return 派单成功的师傅主键，未派出去返回 null
     */
    String autoDispatch(String orderId);

    /**
     * 后台手动指派订单给指定师傅。
     *
     * @param orderId 订单主键
     * @param masterId 师傅主键
     * @param operator 操作人
     * @return 是否成功
     */
    boolean manualDispatch(String orderId, String masterId, String operator);

    /**
     * 评估某订单的候选师傅及评分，不产生派单动作。
     *
     * @param orderId 订单主键
     * @return 候选列表，按评分从高到低
     */
    List<ZjudgDispatchRecord> evaluateCandidates(String orderId);

    /**
     * 批量处理待派单订单，供定时任务调用。
     *
     * @return 本次派单成功的订单数
     */
    int dispatchPendingOrders();

    /**
     * 查询订单的派单记录。
     *
     * @param orderId 订单主键
     * @return 派单记录
     */
    List<ZjudgDispatchRecord> listRecords(String orderId);

    /**
     * 后台查询派单记录。
     *
     * @param query 查询条件
     * @return 派单记录
     */
    List<ZjudgDispatchRecord> selectRecords(ZjudgDispatchRecord query);

    /**
     * 判断订单是否满足派单前置条件。
     *
     * @param order 订单
     * @return 可派单返回 true
     */
    boolean canDispatch(ZjudgOrder order);

    /**
     * 师傅拒绝一个已指派给自己的订单。
     *
     * <p>语义：订单回退到待接单并清空承接师傅，派单记录标记为「被拒」，
     * 拒单次数会计入派单评分的惩罚项。已有派单记录时只回写结果，
     * 保证同一订单同一师傅只有一条有效候选记录。</p>
     *
     * @param orderId 订单主键
     * @param masterId 师傅主键
     * @param reason 拒单原因
     */
    void rejectDispatch(String orderId, String masterId, String reason);
}
