package com.zjude.integrated.map;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.core.domain.AjaxResult;

/**
 * 地图运行时配置接口。
 *
 * 返回的配置仅包含地图公开 Key，不包含后台权限和其他敏感配置。
 */
@RestController
@RequestMapping("/integrated/map")
public class MapRuntimeController
{
    @Resource
    private MapConfigService mapConfigService;

    @GetMapping("/config")
    public AjaxResult config()
    {
        return AjaxResult.success(mapConfigService.getRuntimeConfig());
    }
}
