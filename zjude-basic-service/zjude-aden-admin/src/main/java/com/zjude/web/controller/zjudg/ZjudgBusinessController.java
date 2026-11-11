package com.zjude.web.controller.zjudg;

import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.annotation.Log;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.page.TableDataInfo;
import com.zjude.common.enums.BusinessType;
import com.zjude.system.service.IZjudgBusinessPaymentService;

/** 管理端处理证书申请。 */
@RestController
@RequestMapping("/zjudg/business")
public class ZjudgBusinessController extends BaseController
{
    @Resource private IZjudgBusinessPaymentService service;

    @PreAuthorize("@ss.hasPerm('zjudg:certificate-application:list') and @ss.hasRole('admin')") @GetMapping("/certificate/applications")
    public TableDataInfo applications() { startPage(); return getDataTable(service.listApplications()); }

    @PreAuthorize("@ss.hasPerm('zjudg:certificate-application:edit') and @ss.hasRole('admin')") @Log(title="证书申请", businessType=BusinessType.UPDATE) @PutMapping("/certificate/applications/{id}")
    public AjaxResult updateApplication(@PathVariable String id, @RequestBody UpdateBody body)
    {
        return toAjax(service.updateApplication(id, body.status(), body.trackingNo()));
    }

    public record UpdateBody(String status, String trackingNo) { }
}
