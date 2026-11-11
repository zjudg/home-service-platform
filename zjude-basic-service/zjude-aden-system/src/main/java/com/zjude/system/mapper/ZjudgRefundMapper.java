package com.zjude.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zjude.system.domain.ZjudgRefund;

/**
 * 订单退款数据访问。
 */
public interface ZjudgRefundMapper
{
    /**
     * 新增退款申请。
     *
     * @param item 退款单
     * @return 影响行数
     */
    int insert(ZjudgRefund item);

    /**
     * 按主键查询。
     *
     * @param refundId 退款主键
     * @return 退款单
     */
    ZjudgRefund selectById(@Param("refundId") String refundId);

    /**
     * 按商户退款单号查询，用于回调幂等。
     *
     * @param refundNo 退款单号
     * @return 退款单
     */
    ZjudgRefund selectByRefundNo(@Param("refundNo") String refundNo);

    /**
     * 查询订单最近一条退款单。
     *
     * @param orderId 订单主键
     * @return 退款单，无记录返回 null
     */
    ZjudgRefund selectLatestByOrderId(@Param("orderId") String orderId);

    /**
     * 后台条件查询。
     *
     * @param query 查询条件
     * @return 退款列表
     */
    List<ZjudgRefund> selectList(ZjudgRefund query);

    /**
     * 更新退款状态。仅当当前状态与期望一致时生效，保证回调幂等。
     *
     * @param refundNo 退款单号
     * @param fromStatus 期望的当前状态
     * @param targetStatus 目标状态
     * @param wxRefundId 微信退款单号
     * @param failReason 失败原因
     * @return 影响行数
     */
    int updateStatus(@Param("refundNo") String refundNo, @Param("fromStatus") String fromStatus,
            @Param("targetStatus") String targetStatus, @Param("wxRefundId") String wxRefundId,
            @Param("failReason") String failReason);
}
