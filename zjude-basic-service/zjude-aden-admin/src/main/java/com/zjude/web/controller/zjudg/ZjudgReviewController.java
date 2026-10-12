package com.zjude.web.controller.zjudg;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.zjude.common.annotation.Log;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.page.TableDataInfo;
import com.zjude.common.enums.BusinessType;
import com.zjude.system.domain.ZjudgServiceReview;
import com.zjude.system.service.IZjudgReviewService;

@RestController
@RequestMapping("/zjudg/review")
public class ZjudgReviewController extends BaseController {
    @Resource private IZjudgReviewService service;
    @PreAuthorize("@ss.hasPerm('zjudg:review:list')") @GetMapping("/list") public TableDataInfo list(ZjudgServiceReview query) { startPage(); List<ZjudgServiceReview> rows=service.selectList(query); return getDataTable(rows); }
    @PreAuthorize("@ss.hasPerm('zjudg:review:query')") @GetMapping("/{id}") public AjaxResult get(@PathVariable String id) { return success(service.selectById(id)); }
    @PreAuthorize("@ss.hasPerm('zjudg:review:reply')") @Log(title="服务评价回复",businessType=BusinessType.UPDATE) @PutMapping("/reply") public AjaxResult reply(@RequestBody ZjudgServiceReview item) { item.setUpdateBy(getUsername()); return toAjax(service.updateReply(item)); }
    @PreAuthorize("@ss.hasPerm('zjudg:review:edit')") @Log(title="服务评价状态",businessType=BusinessType.UPDATE) @PutMapping("/status") public AjaxResult status(@RequestBody ZjudgServiceReview item) { item.setUpdateBy(getUsername()); return toAjax(service.updateStatus(item)); }
}
