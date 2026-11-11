package com.zjude.system.service;

import java.util.Map;
import com.zjude.system.domain.ZjudgOrder;

/**
 * 订单支付服务。
 *
 * <p>支付链路：创建支付流水 → 调用微信下单 → 前端唤起支付 → 微信回调 →
 * 校验回调真实性 → 幂等回写订单与流水。</p>
 *
 * <p>回调校验采用「验签优先、查单兜底」：配置了平台证书时严格验签；
 * 未配置时改用主动查单，以微信侧结果为准，避免伪造回调把订单刷成已支付。</p>
 */
public interface IZjudgOrderPaymentService
{
    /**
     * 创建支付单并返回小程序唤起支付所需参数。
     *
     * @param orderId 订单主键
     * @param userId 当前登录用户主键
     * @return 支付参数，可直接传给 uni.requestPayment
     */
    Map<String, Object> createPayment(String orderId, String userId);

    /**
     * 处理微信支付回调。
     *
     * @param body 回调原始报文
     * @param serial 微信支付平台证书序列号请求头
     * @param nonce 随机串请求头
     * @param signature 签名请求头
     * @param timestamp 时间戳请求头
     * @return 处理结果，true 表示已确认支付成功
     */
    boolean handleNotify(String body, String serial, String nonce, String signature, String timestamp);

    /**
     * 主动查询并同步支付结果，用于回调丢失或前端轮询补偿。
     *
     * @param orderId 订单主键
     * @param userId 当前登录用户主键
     * @return 同步后的订单
     */
    ZjudgOrder syncPaymentStatus(String orderId, String userId);
}
