package com.zjude.web.controller.zjudg;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.zjude.common.annotation.Log;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.page.TableDataInfo;
import com.zjude.common.enums.BusinessType;
import com.zjude.system.domain.ZjudgOrder;
import com.zjude.system.service.IZjudgOrderService;

/**
 * 管理端订单管理。
 */
@RestController
@RequestMapping("/zjudg/order")
public class ZjudgOrderController extends BaseController
{
    @Resource
    private IZjudgOrderService service;

    @PreAuthorize("@ss.hasPerm('zjudg:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(ZjudgOrder query)
    {
        startPage();
        List<ZjudgOrder> rows = service.selectList(query);
        return getDataTable(rows);
    }

    @PreAuthorize("@ss.hasPerm('zjudg:order:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable String id)
    {
        return success(service.selectById(id));
    }

    /**
     * 管理端流转订单状态。
     *
     * @param id 订单主键
     * @param orderStatus 目标状态编码
     * @return 操作结果
     */
    @PreAuthorize("@ss.hasPerm('zjudg:order:edit')")
    @Log(title = "家政订单", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/status")
    public AjaxResult updateStatus(@PathVariable String id, @Validated @RequestBody StatusBody orderStatus)
    {
        return toAjax(service.updateStatus(id, orderStatus.orderStatus(), getUsername()));
    }

    /** 状态流转请求体。 */
    public record StatusBody(@jakarta.validation.constraints.NotBlank(message = "目标状态不能为空") String orderStatus)
    {
    }
}
