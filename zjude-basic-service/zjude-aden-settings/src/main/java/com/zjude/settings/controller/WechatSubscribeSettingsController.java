package com.zjude.settings.controller;

import java.util.List;
import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.annotation.Log;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.page.TableDataInfo;
import com.zjude.common.enums.BusinessType;
import com.zjude.settings.service.IPageSettingsService;
import com.zjude.system.domain.SysConfig;

/**
 * 小程序订阅消息设置接口。
 *
 * <p>模板 ID 需在微信公众平台「订阅消息」中申请后填入，留空时推送自动跳过，
 * 不影响下单与履约。</p>
 */
@RestController
@RequestMapping("/manage/settings/wechat-subscribe")
public class WechatSubscribeSettingsController extends BaseController
{
    @Resource
    private IPageSettingsService pageSettingsService;

    @PreAuthorize("@ss.hasPerm('settings:wechat-subscribe:list')")
    @GetMapping("/config/list")
    public TableDataInfo list()
    {
        List<SysConfig> list = pageSettingsService.list("wechatSubscribe");
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPerm('settings:wechat-subscribe:edit')")
    @Log(title = "小程序订阅消息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult saveForm(@RequestBody Map<String, Object> settings)
    {
        pageSettingsService.saveForm("wechatSubscribe", settings, getUsername());
        return success();
    }
}
