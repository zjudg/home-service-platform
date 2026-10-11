package com.zjude.settings.controller;

import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.annotation.Log;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.enums.BusinessType;
import com.zjude.settings.domain.UploadSettingsRequest;
import com.zjude.settings.service.IUploadSettingsService;

/**
 * 上传设置管理。
 */
@RestController
@RequestMapping("/manage/upload")
public class UploadSettingsController extends BaseController
{
    @Resource
    private IUploadSettingsService uploadSettingsService;

    @PreAuthorize("@ss.hasPerm('settings:upload:list')")
    @GetMapping("/settings")
    public AjaxResult getSettings()
    {
        return success(uploadSettingsService.getSettings());
    }

    @PreAuthorize("@ss.hasPerm('settings:upload:edit')")
    @Log(title = "上传设置", businessType = BusinessType.UPDATE)
    @PutMapping("/settings")
    public AjaxResult saveSettings(@Validated @RequestBody UploadSettingsRequest request)
    {
        uploadSettingsService.saveSettings(request, getUsername());
        return success();
    }
}
