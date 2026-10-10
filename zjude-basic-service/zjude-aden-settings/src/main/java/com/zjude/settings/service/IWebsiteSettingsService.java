package com.zjude.settings.service;

import com.zjude.settings.domain.WebsiteSettingsRequest;

/**
 * 网站设置应用服务。
 */
public interface IWebsiteSettingsService
{
    WebsiteSettingsRequest getSettings();

    void saveSettings(WebsiteSettingsRequest request, String username);
}
