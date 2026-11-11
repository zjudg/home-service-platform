package com.zjude.system.service;

import java.util.Map;

/**
 * 数据看板服务。
 *
 * <p>返回结构固定为 summary / sales / orders / newUsers / activeUsers / cashflow / refunds，
 * 前端拿到即可直接渲染，不需要再做补零或聚合。</p>
 */
public interface IZjudgDashboardService
{
    /**
     * 汇总指定区间的经营数据。
     *
     * @param beginDate 开始日期 yyyy-MM-dd，为空时取近 15 天
     * @param endDate 结束日期 yyyy-MM-dd，为空时取今天
     * @return 看板数据
     */
    Map<String, Object> summary(String beginDate, String endDate);
}
