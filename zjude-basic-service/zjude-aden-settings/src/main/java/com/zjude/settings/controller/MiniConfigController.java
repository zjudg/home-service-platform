package com.zjude.settings.controller;

import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.annotation.Anonymous;
import com.zjude.settings.service.IMiniConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/mini/config")
@Tag(name = "小程序配置", description = "小程序启动所需的公开商城配置")
@SecurityRequirements
@Anonymous
public class MiniConfigController
{
    @Resource
    private IMiniConfigService service;

    @GetMapping("/bootstrap")
    @Operation(summary = "获取小程序启动配置", description = "获取启动所需的站点和渠道标识；页面配置请调用对应的独立接口")
    public AjaxResult bootstrap()
    {
        return AjaxResult.success(service.bootstrap());
    }

    @GetMapping("/site")
    @Operation(summary = "获取站点公开配置")
    public AjaxResult site()
    {
        return AjaxResult.success(service.site());
    }

    @GetMapping("/navigation")
    @Operation(summary = "获取底部导航配置")
    public AjaxResult navigation()
    {
        return AjaxResult.success(service.navigation());
    }

    @GetMapping("/home")
    @Operation(summary = "获取首页装修配置")
    public AjaxResult home()
    {
        return AjaxResult.success(service.home());
    }

    @GetMapping("/category")
    @Operation(summary = "获取分类页配置")
    public AjaxResult category()
    {
        return AjaxResult.success(service.category());
    }

    @GetMapping("/profile")
    @Operation(summary = "获取个人中心装修配置")
    public AjaxResult profile()
    {
        return AjaxResult.success(service.profile());
    }

    @GetMapping("/payment/methods")
    @Operation(summary = "获取支付方式")
    public AjaxResult paymentMethods(@RequestParam(required = false) String platform)
    {
        return AjaxResult.success(service.paymentMethods(platform));
    }

    @GetMapping("/upload/policy")
    @Operation(summary = "获取上传策略")
    public AjaxResult uploadPolicy()
    {
        return AjaxResult.success(service.uploadPolicy());
    }
}
