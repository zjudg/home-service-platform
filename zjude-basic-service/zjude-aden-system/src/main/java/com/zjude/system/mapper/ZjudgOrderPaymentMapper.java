package com.zjude.system.mapper;

import org.apache.ibatis.annotations.Param;
import com.zjude.system.domain.ZjudgOrderPayment;

/**
 * 订单支付流水数据访问。
 */
public interface ZjudgOrderPaymentMapper
{
    /**
     * 新增支付流水。
     *
     * @param item 支付流水
     * @return 影响行数
     */
    int insert(ZjudgOrderPayment item);

    /**
     * 按商户订单号查询。
     *
     * @param outTradeNo 商户订单号
     * @return 支付流水，不存在时返回 null
     */
    ZjudgOrderPayment selectByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    /**
     * 按订单主键查询最近一条待支付流水。
     *
     * @param orderId 订单主键
     * @return 支付流水
     */
    ZjudgOrderPayment selectPendingByOrderId(@Param("orderId") String orderId);

    /**
     * 标记支付成功。仅当仍处于待支付时生效，保证回调幂等。
     *
     * @param outTradeNo 商户订单号
     * @param transactionId 微信支付交易号
     * @param notifyRaw 回调报文
     * @return 影响行数
     */
    int markPaid(@Param("outTradeNo") String outTradeNo, @Param("transactionId") String transactionId,
            @Param("notifyRaw") String notifyRaw);

    /**
     * 更新预支付订单号。
     *
     * @param outTradeNo 商户订单号
     * @param prepayId 预支付订单号
     * @return 影响行数
     */
    int updatePrepayId(@Param("outTradeNo") String outTradeNo, @Param("prepayId") String prepayId);
}
