package com.zjude.integrated.app;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.utils.StringUtils;
import com.zjude.system.service.ISysConfigService;

@Service
public class DefaultAppConfigService implements AppConfigService
{
    @Resource
    private ISysConfigService configService;

    @Override
    public AppRuntimeConfig getRuntimeConfig()
    {
        return new AppRuntimeConfig(
                value(AppConfigKeys.IOS_DOWNLOAD_URL),
                value(AppConfigKeys.ANDROID_DOWNLOAD_URL),
                value(AppConfigKeys.DOWNLOAD_TEXT, "欢迎点击下载APP~"),
                enabled(AppConfigKeys.PRIVACY_AGREEMENT),
                enabled(AppConfigKeys.WECHAT_LOGIN));
    }

    private boolean enabled(String key)
    {
        return "1".equals(value(key)) || "true".equalsIgnoreCase(value(key));
    }

    private String value(String key)
    {
        return value(key, "");
    }

    private String value(String key, String fallback)
    {
        String value = configService.selectConfigByKey(key);
        return StringUtils.isEmpty(value) ? fallback : value.trim();
    }
}
