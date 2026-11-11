package com.zjude.system.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 数据看板聚合查询。
 *
 * <p>所有口径都按「业务发生时间」统计：营收取支付流水的 paid_time，
 * 退款取退款单的 create_time，不用订单的更新时间，避免补录数据把历史统计搅乱。</p>
 */
public interface ZjudgDashboardMapper
{
    /**
     * 区间内支付成功的营业额。
     *
     * @param begin 开始时间（含），yyyy-MM-dd HH:mm:ss
     * @param end 结束时间（不含）
     * @return 金额合计，无数据返回 0
     */
    BigDecimal sumPaidAmount(@Param("begin") String begin, @Param("end") String end);

    /**
     * 区间内支付成功的订单数。
     *
     * @param begin 开始时间（含）
     * @param end 结束时间（不含）
     * @return 订单数
     */
    int countPaidOrders(@Param("begin") String begin, @Param("end") String end);

    /**
     * 区间内退款成功的金额合计。
     *
     * @param begin 开始时间（含）
     * @param end 结束时间（不含）
     * @return 金额合计，无数据返回 0
     */
    BigDecimal sumRefundedAmount(@Param("begin") String begin, @Param("end") String end);

    /**
     * 区间内待处理退款金额（待审核 + 退款中）。
     *
     * @param begin 开始时间（含）
     * @param end 结束时间（不含）
     * @return 金额合计，无数据返回 0
     */
    BigDecimal sumPendingRefundAmount(@Param("begin") String begin, @Param("end") String end);

    /**
     * 按天统计支付金额。
     *
     * @param begin 开始时间（含）
     * @param end 结束时间（不含）
     * @return 每行包含 stat_date、amount
     */
    List<Map<String, Object>> selectDailyPaidAmount(@Param("begin") String begin, @Param("end") String end);

    /**
     * 按天统计支付成功的订单数。
     *
     * @param begin 开始时间（含）
     * @param end 结束时间（不含）
     * @return 每行包含 stat_date、total
     */
    List<Map<String, Object>> selectDailyPaidOrders(@Param("begin") String begin, @Param("end") String end);

    /**
     * 按天统计新增用户数。
     *
     * @param begin 开始时间（含）
     * @param end 结束时间（不含）
     * @return 每行包含 stat_date、total
     */
    List<Map<String, Object>> selectDailyNewUsers(@Param("begin") String begin, @Param("end") String end);

    /**
     * 按天统计登录成功的去重活跃用户数。
     *
     * @param begin 开始时间（含）
     * @param end 结束时间（不含）
     * @return 每行包含 stat_date、total
     */
    List<Map<String, Object>> selectDailyActiveUsers(@Param("begin") String begin, @Param("end") String end);

    /**
     * 按天统计退款成功金额。
     *
     * @param begin 开始时间（含）
     * @param end 结束时间（不含）
     * @return 每行包含 stat_date、amount
     */
    List<Map<String, Object>> selectDailyRefundedAmount(@Param("begin") String begin, @Param("end") String end);

    /**
     * 退款状态分布。
     *
     * @param begin 开始时间（含）
     * @param end 结束时间（不含）
     * @return 每行包含 refund_status、total、amount
     */
    List<Map<String, Object>> selectRefundStatusDistribution(@Param("begin") String begin, @Param("end") String end);

    /**
     * 区间内已结算给师傅的金额。
     *
     * @param begin 开始时间（含）
     * @param end 结束时间（不含）
     * @return 金额合计，无数据返回 0
     */
    BigDecimal sumMasterIncome(@Param("begin") String begin, @Param("end") String end);
}
