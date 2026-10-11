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
import com.zjude.integrated.sms.SmsScene;
import com.zjude.integrated.sms.SmsService;
import com.zjude.settings.service.SmsSettingsService;
import com.zjude.system.domain.SysConfig;

@RestController
@RequestMapping("/manage/settings/sms")
public class SmsSettingsController extends BaseController
{
    @Resource
    private SmsSettingsService smsSettingsService;

    @Resource
    private SmsService smsService;

    @PreAuthorize("@ss.hasPerm('settings:sms:list')")
    @GetMapping("/config/list")
    public TableDataInfo list()
    {
        List<SysConfig> configs = smsSettingsService.list();
        return getDataTable(configs);
    }

    @PreAuthorize("@ss.hasPerm('settings:sms:edit')")
    @Log(title = "短信配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult save(@RequestBody Map<String, Object> settings)
    {
        smsSettingsService.save(settings, getUsername());
        return success();
    }

    @PreAuthorize("@ss.hasPerm('settings:sms:edit')")
    @PutMapping("/validate")
    public AjaxResult validate()
    {
        smsSettingsService.validate();
        return success();
    }

    @PreAuthorize("@ss.hasPerm('settings:sms:edit')")
    @PutMapping("/test")
    public AjaxResult test(@RequestBody Map<String, String> request)
    {
        smsService.sendCode(request.get("mobile"), SmsScene.LOGIN, request.get("code"));
        return success();
    }
}
