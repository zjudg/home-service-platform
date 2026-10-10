package com.zjude.settings.controller;

import java.util.List;
import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
 * 页面配置接口。
 */
@RestController
@RequestMapping("/manage/settings")
public class PageSettingsController extends BaseController
{
    @Resource
    private IPageSettingsService pageSettingsService;

    @PreAuthorize("@ss.hasPerm('settings:' + #page + ':list')")
    @GetMapping("/{page}/config/list")
    public TableDataInfo list(@PathVariable String page)
    {
        List<SysConfig> list = pageSettingsService.list(page);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPerm('settings:' + #page + ':edit')")
    @Log(title = "页面配置", businessType = BusinessType.UPDATE)
    @PutMapping("/{page}")
    public AjaxResult saveForm(@PathVariable String page, @RequestBody Map<String, Object> settings)
    {
        pageSettingsService.saveForm(page, settings, getUsername());
        return success();
    }

}
