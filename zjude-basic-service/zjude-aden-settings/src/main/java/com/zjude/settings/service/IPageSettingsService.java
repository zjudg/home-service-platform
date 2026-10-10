package com.zjude.settings.service;

import java.util.List;
import java.util.Map;
import com.zjude.system.domain.SysConfig;

/**
 * 页面配置应用服务。
 */
public interface IPageSettingsService
{
    /**
     * 查询指定页面的配置。
     *
     * @param page 页面标识
     * @return 页面配置
     */
    List<SysConfig> list(String page);

    /**
     * 保存页面表单。
     *
     * @param page 页面标识
     * @param settings 页面表单字段
     * @param username 操作人
     */
    void saveForm(String page, Map<String, Object> settings, String username);
}
