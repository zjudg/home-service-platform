package com.zjude.settings.service;

import java.util.List;
import java.util.Map;
import com.zjude.system.domain.SysConfig;

public interface SmsSettingsService
{
    List<SysConfig> list();

    void save(Map<String, Object> settings, String username);

    void validate();
}
