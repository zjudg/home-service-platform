package com.zjude.settings.service.impl;

import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zjude.common.exception.ServiceException;
import com.zjude.settings.domain.WebsiteSettingsRequest;
import com.zjude.settings.service.IWebsiteSettingsService;
import com.zjude.system.domain.SysConfig;
import com.zjude.system.service.ISysConfigService;

/**
 * 网站设置应用服务。
 */
@Service
public class WebsiteSettingsServiceImpl implements IWebsiteSettingsService
{
    private static final String PREFIX = "website.";

    @Resource
    private ISysConfigService configService;

    @Override
    public WebsiteSettingsRequest getSettings()
    {
        Map<String, String> values = values();
        WebsiteSettingsRequest request = new WebsiteSettingsRequest();
        request.setShopName(values.get("website.shopName"));
        request.setFavicon(values.get("website.favicon"));
        request.setMobileLoginLogo(values.get("website.mobileLoginLogo"));
        request.setMobileLogo(values.get("website.mobileLogo"));
        request.setPcLogo(values.get("website.pcLogo"));
        request.setAdminLoginLogo(values.get("website.adminLoginLogo"));
        request.setAdminHomeImage(values.get("website.adminHomeImage"));
        request.setAdminLoginName(values.get("website.adminLoginName"));
        request.setAdminLogo(values.get("website.adminLogo"));
        request.setMemberDefaultAvatar(values.get("website.memberDefaultAvatar"));
        request.setGoodsDefaultImage(values.get("website.goodsDefaultImage"));
        return request;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSettings(WebsiteSettingsRequest request, String username)
    {
        if (request == null)
        {
            throw new ServiceException("网站设置不能为空");
        }
        Map<String, SysConfig> records = records();
        save(records, "website.shopName", "商城名称", request.getShopName(), username);
        save(records, "website.favicon", "商城浏览器图标", request.getFavicon(), username);
        save(records, "website.mobileLoginLogo", "移动端登录页Logo", request.getMobileLoginLogo(), username);
        save(records, "website.mobileLogo", "移动端商城Logo", request.getMobileLogo(), username);
        save(records, "website.pcLogo", "PC端商城Logo", request.getPcLogo(), username);
        save(records, "website.adminLoginLogo", "管理后台登录页Logo", request.getAdminLoginLogo(), username);
        save(records, "website.adminHomeImage", "管理后台首页主题", request.getAdminHomeImage(), username);
        save(records, "website.adminLoginName", "管理后台登录页名称", request.getAdminLoginName(), username);
        save(records, "website.adminLogo", "管理后台Logo", request.getAdminLogo(), username);
        save(records, "website.memberDefaultAvatar", "会员默认头像", request.getMemberDefaultAvatar(), username);
        save(records, "website.goodsDefaultImage", "商品默认封面", request.getGoodsDefaultImage(), username);
        configService.resetConfigCache();
    }

    private Map<String, String> values()
    {
        Map<String, String> values = new HashMap<>();
        for (SysConfig config : configService.selectConfigList(new SysConfig()))
        {
            if (config.getConfigKey() != null && config.getConfigKey().startsWith(PREFIX))
            {
                values.put(config.getConfigKey(), config.getConfigValue());
            }
        }
        return values;
    }

    private Map<String, SysConfig> records()
    {
        Map<String, SysConfig> records = new HashMap<>();
        for (SysConfig config : configService.selectConfigList(new SysConfig()))
        {
            if (config.getConfigKey() != null && config.getConfigKey().startsWith(PREFIX))
            {
                records.put(config.getConfigKey(), config);
            }
        }
        return records;
    }

    private void save(Map<String, SysConfig> records, String key, String name, String value, String username)
    {
        SysConfig config = records.get(key);
        if (config == null)
        {
            config = new SysConfig();
            config.setConfigName("网站设置-" + name);
            config.setConfigKey(key);
            config.setConfigType("N");
            config.setCreateBy(username);
            config.setRemark("网站设置");
            config.setConfigValue(value == null ? "" : value.trim());
            configService.insertConfig(config);
        }
        else
        {
            config.setConfigValue(value == null ? "" : value.trim());
            config.setUpdateBy(username);
            configService.updateConfig(config);
        }
    }
}
