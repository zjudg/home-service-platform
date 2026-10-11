package com.zjude.settings.controller;

import java.util.List;
import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.annotation.Log;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.page.TableDataInfo;
import com.zjude.common.enums.BusinessType;
import com.zjude.settings.service.PaymentSettingsService;
import com.zjude.system.domain.SysConfig;

@RestController
@RequestMapping("/manage/settings/payment")
public class PaymentSettingsController extends BaseController
{
    @Resource
    private PaymentSettingsService paymentSettingsService;

    @PreAuthorize("@ss.hasPerm('settings:payment:list')")
    @GetMapping("/config/list")
    public TableDataInfo list()
    {
        List<SysConfig> configs = paymentSettingsService.list();
        return getDataTable(configs);
    }

    @PreAuthorize("@ss.hasPerm('settings:payment:edit')")
    @Log(title = "支付配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult save(@RequestBody Map<String, Object> settings)
    {
        paymentSettingsService.save(settings, getUsername());
        return success();
    }

    @PreAuthorize("@ss.hasPerm('settings:payment:edit')")
    @PutMapping("/validate")
    public AjaxResult validate(@RequestBody Map<String, Object> request)
    {
        paymentSettingsService.validate(String.valueOf(request.get("provider")));
        return success();
    }
}
