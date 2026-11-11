package com.zjude.system.service;

import java.math.BigDecimal;
import java.util.List;
import com.zjude.system.domain.ZjudgRefund;

/**
 * 订单退款服务。
 *
 * <p>流程分两步：用户申请 → 平台审核 → 提交微信退款。
 * 只有「已支付且未退款」的订单可申请退款；同一订单同时只允许存在一条进行中的退款单。</p>
 */
public interface IZjudgRefundService
{
    /**
     * 用户申请退款。
     *
     * @param orderId 订单主键
     * @param userId 当前登录用户主键
     * @param reason 申请原因
     * @return 退款单
     */
    ZjudgRefund applyRefund(String orderId, String userId, String reason);

    /**
     * 查询订单最近一条退款单，供用户端展示进度。
     *
     * @param orderId 订单主键
     * @param userId 当前登录用户主键
     * @return 退款单，无记录返回 null
     */
    ZjudgRefund getByOrder(String orderId, String userId);

    /**
     * 后台查询退款列表。
     *
     * @param query 查询条件
     * @return 退款列表
     */
    List<ZjudgRefund> selectList(ZjudgRefund query);

    /**
     * 后台审核同意并提交微信退款。
     *
     * @param refundId 退款主键
     * @param operator 操作人
     * @return 提交后的退款单
     */
    ZjudgRefund approve(String refundId, String operator);

    /**
     * 后台拒绝退款申请。
     *
     * @param refundId 退款主键
     * @param reason 拒绝原因
     * @param operator 操作人
     * @return 影响行数
     */
    int reject(String refundId, String reason, String operator);

    /**
     * 主动查询并同步退款结果，用于回调丢失补偿。
     *
     * @param refundId 退款主键
     * @return 同步后的退款单
     */
    ZjudgRefund syncStatus(String refundId);

    /**
     * 处理微信退款回调。
     *
     * @param refundNo 商户退款单号
     * @param wxRefundId 微信退款单号
     * @param success 是否退款成功
     * @param failReason 失败原因
     * @return 是否处理成功
     */
    boolean handleNotify(String refundNo, String wxRefundId, boolean success, String failReason);

    /**
     * 回调无法验签时按退款单号主动查单，以微信侧结果为准。
     *
     * @param refundNo 商户退款单号
     * @return 是否已确认退款成功
     */
    boolean confirmByQuery(String refundNo);

    /**
     * 计算订单可退款金额。
     *
     * @param orderId 订单主键
     * @return 可退金额，不可退时为 0
     */
    /**
     * 计算订单可退款金额（用户自助退款口径）。
     *
     * <p>已完成订单不在此口径内，由平台人工处理。</p>
     *
     * @param orderId 订单主键
     * @return 可退金额，不可退时为 0
     */
    BigDecimal refundableAmount(String orderId);
}
