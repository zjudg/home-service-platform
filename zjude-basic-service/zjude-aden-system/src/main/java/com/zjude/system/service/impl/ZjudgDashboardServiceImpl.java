package com.zjude.system.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;
import com.zjude.system.mapper.ZjudgDashboardMapper;
import com.zjude.system.service.IZjudgDashboardService;

/**
 * 数据看板服务实现。
 *
 * <p>补齐缺失日期是这里的主要职责：聚合 SQL 只返回有数据的日期，
 * 直接给前端画图会出现横跳的时间轴，因此统一在服务层按天补零。</p>
 */
@Service
public class ZjudgDashboardServiceImpl implements IZjudgDashboardService
{
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 默认统计窗口：近 15 天 */
    private static final int DEFAULT_DAYS = 15;

    /** 允许的最大跨度，防止一次查询把整表扫穿 */
    private static final int MAX_DAYS = 180;

    /** 支付成功 */
    private static final String PAY_SUCCESS = "1";

    /** 退款状态：待审核 / 退款中 / 成功 / 失败 / 已拒绝 */
    private static final String REFUND_PENDING = "0";
    private static final String REFUND_PROCESSING = "1";
    private static final String REFUND_SUCCESS = "2";
    private static final String REFUND_FAILED = "3";
    private static final String REFUND_REJECTED = "4";

    @Resource
    private ZjudgDashboardMapper dashboardMapper;

    @Override
    public Map<String, Object> summary(String beginDate, String endDate)
    {
        LocalDate end = parseDate(endDate, LocalDate.now());
        LocalDate begin = parseDate(beginDate, end.minusDays(DEFAULT_DAYS - 1L));
        if (begin.isAfter(end))
        {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        long span = ChronoUnit.DAYS.between(begin, end) + 1;
        if (span > MAX_DAYS)
        {
            throw new ServiceException("统计区间不能超过 " + MAX_DAYS + " 天");
        }

        String beginTime = begin.format(DATE) + " 00:00:00";
        String endTime = end.plusDays(1).format(DATE) + " 00:00:00";

        List<LocalDate> days = new ArrayList<>();
        for (LocalDate cursor = begin; !cursor.isAfter(end); cursor = cursor.plusDays(1))
        {
            days.add(cursor);
        }

        Map<String, BigDecimal> paidByDay = amountMap(dashboardMapper.selectDailyPaidAmount(beginTime, endTime));
        Map<String, BigDecimal> orderByDay = amountMap(dashboardMapper.selectDailyPaidOrders(beginTime, endTime));
        Map<String, Integer> newUsersByDay = countMap(dashboardMapper.selectDailyNewUsers(beginTime, endTime));
        Map<String, Integer> activeUsersByDay = countMap(dashboardMapper.selectDailyActiveUsers(beginTime, endTime));
        Map<String, BigDecimal> refundByDay = amountMap(dashboardMapper.selectDailyRefundedAmount(beginTime, endTime));

        BigDecimal totalPaid = scale(dashboardMapper.sumPaidAmount(beginTime, endTime));
        BigDecimal totalRefunded = scale(dashboardMapper.sumRefundedAmount(beginTime, endTime));
        BigDecimal totalPending = scale(dashboardMapper.sumPendingRefundAmount(beginTime, endTime));
        BigDecimal totalMasterIncome = scale(dashboardMapper.sumMasterIncome(beginTime, endTime));
        int orders = dashboardMapper.countPaidOrders(beginTime, endTime);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("beginDate", begin.format(DATE));
        result.put("endDate", end.format(DATE));
        result.put("days", days.stream().map(day -> day.format(DATE)).toList());

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("revenue", totalPaid);
        summary.put("orders", orders);
        summary.put("refunded", totalRefunded);
        // 净营业额 = 收取金额 - 已退金额，退款后不应仍记为收入
        summary.put("netRevenue", totalPaid.subtract(totalRefunded));
        summary.put("pendingRefund", totalPending);
        summary.put("masterIncome", totalMasterIncome);
        // 平台毛利 = 净营业额 - 结算给师傅的部分
        summary.put("platformIncome", totalPaid.subtract(totalRefunded).subtract(totalMasterIncome));
        summary.put("avgOrderAmount", orders == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : totalPaid.divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP));
        result.put("summary", summary);

        result.put("sales", fillByDay(days, paidByDay));
        result.put("orders", fillByDay(days, orderByDay));
        result.put("newUsers", fillCountByDay(days, newUsersByDay));
        result.put("activeUsers", fillCountByDay(days, activeUsersByDay));
        result.put("cashflow", buildCashflow(days, paidByDay, refundByDay));
        result.put("refunds", buildRefundDistribution(dashboardMapper.selectRefundStatusDistribution(beginTime, endTime)));

        return result;
    }

    private List<BigDecimal> fillByDay(List<LocalDate> days, Map<String, BigDecimal> values)
    {
        List<BigDecimal> rows = new ArrayList<>(days.size());
        for (LocalDate day : days)
        {
            rows.add(values.getOrDefault(day.format(DATE), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)));
        }
        return rows;
    }

    private List<Integer> fillCountByDay(List<LocalDate> days, Map<String, Integer> values)
    {
        List<Integer> rows = new ArrayList<>(days.size());
        for (LocalDate day : days)
        {
            rows.add(values.getOrDefault(day.format(DATE), 0));
        }
        return rows;
    }

    private Map<String, Object> buildCashflow(List<LocalDate> days, Map<String, BigDecimal> paid,
            Map<String, BigDecimal> refunded)
    {
        Map<String, Object> cashflow = new LinkedHashMap<>();
        cashflow.put("revenue", fillByDay(days, paid));
        cashflow.put("refund", fillByDay(days, refunded));
        return cashflow;
    }

    /**
     * 退款状态分布。固定输出四个状态，缺失的补 0，
     * 保证饼图图例稳定，不会因为某天没有失败单而少一块颜色。
     */
    private List<Map<String, Object>> buildRefundDistribution(List<Map<String, Object>> rows)
    {
        Map<String, Map<String, Object>> byStatus = new LinkedHashMap<>();
        for (Map<String, Object> row : rows)
        {
            String status = String.valueOf(row.get("refund_status"));
            byStatus.put(status, row);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        result.add(distributionItem("待审核", REFUND_PENDING, byStatus.get(REFUND_PENDING)));
        result.add(distributionItem("退款中", REFUND_PROCESSING, byStatus.get(REFUND_PROCESSING)));
        result.add(distributionItem("退款成功", REFUND_SUCCESS, byStatus.get(REFUND_SUCCESS)));
        // 失败与拒绝合并展示，运营视角都是「没退成」
        Map<String, Object> failed = byStatus.get(REFUND_FAILED);
        Map<String, Object> rejected = byStatus.get(REFUND_REJECTED);
        result.add(distributionItem("退款失败", REFUND_FAILED, merge(failed, rejected)));
        return result;
    }

    private Map<String, Object> distributionItem(String name, String status, Map<String, Object> row)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("status", status);
        item.put("value", row == null ? 0 : toInt(row.get("total")));
        item.put("amount", row == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : scale(toDecimal(row.get("amount"))));
        return item;
    }

    private Map<String, Object> merge(Map<String, Object> first, Map<String, Object> second)
    {
        if (first == null)
        {
            return second;
        }
        if (second == null)
        {
            return first;
        }
        Map<String, Object> merged = new LinkedHashMap<>();
        merged.put("total", toInt(first.get("total")) + toInt(second.get("total")));
        merged.put("amount", toDecimal(first.get("amount")).add(toDecimal(second.get("amount"))));
        return merged;
    }

    /** 把按天聚合结果转成日期到数值的映射，空值按 0 处理 */
    private Map<String, BigDecimal> amountMap(List<Map<String, Object>> rows)
    {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Map<String, Object> row : rows)
        {
            String date = String.valueOf(row.get("stat_date"));
            // 金额可能来自 sum()，订单数来自 count()，两者都用同一条通道转成 BigDecimal
            BigDecimal value = row.get("amount") != null
                    ? toDecimal(row.get("amount"))
                    : BigDecimal.valueOf(toInt(row.get("total")));
            map.put(date, scale(value));
        }
        return map;
    }

    private Map<String, Integer> countMap(List<Map<String, Object>> rows)
    {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Map<String, Object> row : rows)
        {
            map.put(String.valueOf(row.get("stat_date")), toInt(row.get("total")));
        }
        return map;
    }

    private LocalDate parseDate(String value, LocalDate fallback)
    {
        if (StringUtils.isEmpty(value))
        {
            return fallback;
        }
        try
        {
            return LocalDate.parse(value.trim(), DATE);
        }
        catch (DateTimeParseException e)
        {
            throw new ServiceException("日期格式不正确，应为 yyyy-MM-dd");
        }
    }

    private BigDecimal toDecimal(Object value)
    {
        if (value == null)
        {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal)
        {
            return decimal;
        }
        return new BigDecimal(String.valueOf(value));
    }

    private int toInt(Object value)
    {
        if (value == null)
        {
            return 0;
        }
        return value instanceof Number number ? number.intValue() : Integer.parseInt(String.valueOf(value));
    }

    private BigDecimal scale(BigDecimal value)
    {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
}
