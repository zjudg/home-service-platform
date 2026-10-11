package com.zjude.integrated.map;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.utils.StringUtils;
import com.zjude.system.service.ISysConfigService;

@Service
public class DefaultMapConfigService implements MapConfigService
{
    @Resource
    private ISysConfigService configService;

    @Override
    public MapRuntimeConfig getRuntimeConfig()
    {
        return new MapRuntimeConfig(
                value(MapConfigKeys.TENCENT_KEY),
                value(MapConfigKeys.TIANDITU_KEY));
    }

    private String value(String key)
    {
        String value = configService.selectConfigByKey(key);
        return StringUtils.isEmpty(value) ? "" : value.trim();
    }
}
