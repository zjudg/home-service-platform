package com.zjude.settings.service;

import com.zjude.settings.domain.UploadSettingsRequest;

/**
 * 上传设置应用服务。
 */
public interface IUploadSettingsService
{
    UploadSettingsRequest getSettings();

    void saveSettings(UploadSettingsRequest request, String username);
}
