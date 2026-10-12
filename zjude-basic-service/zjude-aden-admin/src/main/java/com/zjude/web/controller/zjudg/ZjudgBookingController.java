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
import com.zjude.system.domain.ZjudgBookingSlot;
import com.zjude.system.service.IZjudgBookingService;

@RestController
@RequestMapping("/zjudg/booking")
public class ZjudgBookingController extends BaseController {
    @Resource private IZjudgBookingService service;
    @PreAuthorize("@ss.hasPerm('zjudg:booking:list')") @GetMapping("/list") public TableDataInfo list(ZjudgBookingSlot query) { startPage(); List<ZjudgBookingSlot> rows=service.selectList(query); return getDataTable(rows); }
    @PreAuthorize("@ss.hasPerm('zjudg:booking:query')") @GetMapping("/{id}") public AjaxResult get(@PathVariable String id) { return success(service.selectById(id)); }
    @PreAuthorize("@ss.hasPerm('zjudg:booking:add')") @Log(title="预约时间段",businessType=BusinessType.INSERT) @PostMapping public AjaxResult add(@Validated @RequestBody ZjudgBookingSlot item) { item.setCreateBy(getUsername()); return toAjax(service.insert(item)); }
    @PreAuthorize("@ss.hasPerm('zjudg:booking:edit')") @Log(title="预约时间段",businessType=BusinessType.UPDATE) @PutMapping public AjaxResult edit(@Validated @RequestBody ZjudgBookingSlot item) { item.setUpdateBy(getUsername()); return toAjax(service.update(item)); }
    @PreAuthorize("@ss.hasPerm('zjudg:booking:remove')") @Log(title="预约时间段",businessType=BusinessType.DELETE) @DeleteMapping("/{ids}") public AjaxResult remove(@PathVariable String[] ids) { return toAjax(service.deleteByIds(ids)); }
}
