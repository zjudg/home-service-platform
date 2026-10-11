package com.zjude.settings.controller;

import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.settings.service.IWebsiteSettingsService;

/**
 * 网站设置接口。
 */
@RestController
@RequestMapping("/manage/settings/website")
public class WebsiteSettingsController extends BaseController
{
    @Resource
    private IWebsiteSettingsService websiteSettingsService;

    @PreAuthorize("@ss.hasPerm('settings:website:list')")
    @GetMapping
    public AjaxResult getSettings()
    {
        return success(websiteSettingsService.getSettings());
    }

}
