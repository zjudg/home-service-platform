package com.zjude.web.controller.monitor;

import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.annotation.Log;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.page.TableDataInfo;
import com.zjude.common.enums.BusinessType;
import com.zjude.common.utils.poi.ExcelUtil;
import com.zjude.framework.web.service.SysPasswordService;
import com.zjude.system.domain.SysLoginInfo;
import com.zjude.system.service.ISysLoginInfoService;

/**
 * 系统访问记录
 * 
 * @author bai
 */
@RestController
@RequestMapping("/monitor/login/information")
public class SysLoginInfoController extends BaseController
{
    @Resource
    private ISysLoginInfoService loginInfoService;

    @Resource
    private SysPasswordService passwordService;

    @PreAuthorize("@ss.hasPerm('monitor:login-info:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysLoginInfo loginInfo)
    {
        startPage();
        List<SysLoginInfo> list = loginInfoService.selectLoginInfoList(loginInfo);
        return getDataTable(list);
    }

    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPerm('monitor:login-info:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysLoginInfo loginInfo)
    {
        List<SysLoginInfo> list = loginInfoService.selectLoginInfoList(loginInfo);
        ExcelUtil<SysLoginInfo> util = new ExcelUtil<>(SysLoginInfo.class);
        util.exportExcel(response, list, "登录日志");
    }

    @PreAuthorize("@ss.hasPerm('monitor:login-info:remove')")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{infoIds}")
    public AjaxResult remove(@PathVariable Long[] infoIds)
    {
        return toAjax(loginInfoService.deleteLoginInfoByIds(infoIds));
    }

    @PreAuthorize("@ss.hasPerm('monitor:login-info:remove')")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public AjaxResult clean()
    {
        loginInfoService.cleanLoginInfo();
        return success();
    }

    @PreAuthorize("@ss.hasPerm('monitor:login-info:unlock')")
    @Log(title = "账户解锁", businessType = BusinessType.OTHER)
    @GetMapping("/unlock/{userName}")
    public AjaxResult unlock(@PathVariable("userName") String userName)
    {
        passwordService.clearLoginRecordCache(userName);
        return success();
    }
}
