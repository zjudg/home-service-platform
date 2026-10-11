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
import com.zjude.settings.service.INotificationSettingsService;
import com.zjude.system.domain.SysConfig;

/**
 * 通知设置接口。
 */
@RestController
@RequestMapping("/manage/settings/notification")
public class NotificationSettingsController extends BaseController
{
    @Resource
    private INotificationSettingsService notificationSettingsService;

    @PreAuthorize("@ss.hasPerm('settings:notification:list')")
    @GetMapping("/config/list")
    public TableDataInfo list()
    {
        List<SysConfig> list = notificationSettingsService.list();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPerm('settings:notification:edit')")
    @Log(title = "通知设置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult saveForm(@RequestBody Map<String, Object> settings)
    {
        notificationSettingsService.saveForm(settings, getUsername());
        return success();
    }
}
