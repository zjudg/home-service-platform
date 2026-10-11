package com.zjude.settings.service;

import java.util.List;
import java.util.Map;
import com.zjude.system.domain.SysConfig;

public interface INotificationSettingsService
{
    List<SysConfig> list();

    void saveForm(Map<String, Object> settings, String username);
}
