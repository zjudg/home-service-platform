package com.zjude.settings.service.impl;

import java.util.List;
import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.settings.service.IPageSettingsService;
import com.zjude.settings.service.INotificationSettingsService;
import com.zjude.system.domain.SysConfig;

@Service
public class NotificationSettingsServiceImpl implements INotificationSettingsService
{
    @Resource
    private IPageSettingsService pageSettingsService;

    @Override
    public List<SysConfig> list()
    {
        return pageSettingsService.list("notification");
    }

    @Override
    public void saveForm(Map<String, Object> settings, String username)
    {
        pageSettingsService.saveForm("notification", settings, username);
    }
}
