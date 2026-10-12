package com.zjude.web.controller.zjudg;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.page.TableDataInfo;
import com.zjude.system.domain.ZjudgOrder;
import com.zjude.system.service.IZjudgOrderService;

@RestController
@RequestMapping("/zjudg/order")
public class ZjudgOrderController extends BaseController {
    @Resource private IZjudgOrderService service;
    @PreAuthorize("@ss.hasPerm('zjudg:order:list')") @GetMapping("/list") public TableDataInfo list(ZjudgOrder query) { startPage(); List<ZjudgOrder> rows=service.selectList(query); return getDataTable(rows); }
    @PreAuthorize("@ss.hasPerm('zjudg:order:query')") @GetMapping("/{id}") public AjaxResult get(@PathVariable String id) { return success(service.selectById(id)); }
}
