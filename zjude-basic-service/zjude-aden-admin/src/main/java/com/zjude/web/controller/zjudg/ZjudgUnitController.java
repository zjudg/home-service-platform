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
import com.zjude.system.domain.ZjudgServiceUnit;
import com.zjude.system.service.IZjudgUnitService;

@RestController
@RequestMapping("/zjudg/unit")
public class ZjudgUnitController extends BaseController {
    @Resource private IZjudgUnitService service;
    @PreAuthorize("@ss.hasPerm('zjudg:unit:list')") @GetMapping("/list") public TableDataInfo list(ZjudgServiceUnit query) { startPage(); List<ZjudgServiceUnit> rows=service.selectList(query); return getDataTable(rows); }
    @PreAuthorize("@ss.hasPerm('zjudg:unit:query')") @GetMapping("/{id}") public AjaxResult get(@PathVariable String id) { return success(service.selectById(id)); }
    @PreAuthorize("@ss.hasPerm('zjudg:unit:add')") @Log(title="服务单位",businessType=BusinessType.INSERT) @PostMapping public AjaxResult add(@Validated @RequestBody ZjudgServiceUnit item) { item.setCreateBy(getUsername()); return toAjax(service.insert(item)); }
    @PreAuthorize("@ss.hasPerm('zjudg:unit:edit')") @Log(title="服务单位",businessType=BusinessType.UPDATE) @PutMapping public AjaxResult edit(@Validated @RequestBody ZjudgServiceUnit item) { item.setUpdateBy(getUsername()); return toAjax(service.update(item)); }
    @PreAuthorize("@ss.hasPerm('zjudg:unit:remove')") @Log(title="服务单位",businessType=BusinessType.DELETE) @DeleteMapping("/{ids}") public AjaxResult remove(@PathVariable String[] ids) { return toAjax(service.deleteByIds(ids)); }
}
