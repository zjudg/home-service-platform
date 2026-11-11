package com.zjude.system.service;

import com.zjude.system.domain.ZjudgOrder;

/**
 * 家政订单相关的消息通知。
 *
 * <p>实现必须做到「尽力而为」：任何推送失败都只记录日志，不得抛出异常或影响调用方事务。
 * 未配置模板、关闭开关、用户未授权时静默跳过。</p>
 */
public interface IZjudgNotifyService
{
    /**
     * 通知管理员：用户已提交新订单（可能仍待支付）。
     *
     * @param order 订单
     */
    void notifyOrderCreatedToAdmins(ZjudgOrder order);

    /**
     * 通知用户：订单已被师傅接单。
     *
     * @param order 订单
     */
    void notifyAccepted(ZjudgOrder order);

    /**
     * 通知管理员：师傅已接单。
     *
     * @param order 订单
     */
    void notifyAcceptedToAdmins(ZjudgOrder order);

    /**
     * 通知用户：服务已完成，请确认并评价。
     *
     * @param order 订单
     */
    void notifyFinished(ZjudgOrder order);

    /**
     * 通知用户：支付成功。
     *
     * @param order 订单
     */
    void notifyPaid(ZjudgOrder order);

    /**
     * 通知师傅：有新的订单指派。
     *
     * @param masterId 师傅主键
     * @param order 订单
     */
    void notifyNewOrderToMaster(String masterId, ZjudgOrder order);
}
