package com.zjude.web.controller.zjudg;

import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.system.service.IZjudgDashboardService;

/**
 * 数据看板后台接口。
 */
@RestController
@RequestMapping("/zjudg/finance")
public class ZjudgDashboardController extends BaseController
{
    @Resource
    private IZjudgDashboardService dashboardService;

    /**
     * 汇总数据看板指标。
     *
     * @param beginDate 开始日期 yyyy-MM-dd，为空取近 15 天
     * @param endDate 结束日期 yyyy-MM-dd，为空取今天
     * @return summary / sales / orders / newUsers / activeUsers / cashflow / refunds
     */
    @PreAuthorize("@ss.hasPerm('zjudg:finance:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard(@RequestParam(required = false) String beginDate,
            @RequestParam(required = false) String endDate)
    {
        Map<String, Object> data = dashboardService.summary(beginDate, endDate);
        return success(data);
    }
}
