package com.zjude.integrated.app;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.core.domain.AjaxResult;

/**
 * 移动端启动配置接口，不返回管理端权限数据和敏感配置。
 */
@RestController
@RequestMapping("/integrated/app")
public class AppRuntimeController
{
    @Resource
    private AppConfigService appConfigService;

    @GetMapping("/config")
    public AjaxResult config()
    {
        return AjaxResult.success(appConfigService.getRuntimeConfig());
    }
}
