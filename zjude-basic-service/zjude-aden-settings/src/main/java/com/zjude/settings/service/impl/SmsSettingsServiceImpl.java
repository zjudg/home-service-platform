package com.zjude.settings.service.impl;

import java.util.List;
import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.integrated.sms.SmsConfigKeys;
import com.zjude.integrated.sms.SmsScene;
import com.zjude.integrated.sms.SmsService;
import com.zjude.settings.service.IPageSettingsService;
import com.zjude.settings.service.SmsSettingsService;
import com.zjude.system.domain.SysConfig;
import com.zjude.system.service.ISysConfigService;
import com.zjude.common.exception.ServiceException;

@Service
public class SmsSettingsServiceImpl implements SmsSettingsService
{
    @Resource
    private IPageSettingsService pageSettingsService;

    @Resource
    private ISysConfigService configService;

    @Resource
    private SmsService smsService;

    @Override
    public List<SysConfig> list()
    {
        return pageSettingsService.list("sms");
    }

    @Override
    public void save(Map<String, Object> settings, String username)
    {
        pageSettingsService.saveForm("sms", settings, username);
    }

    @Override
    public void validate()
    {
        if (!"1".equals(value(SmsConfigKeys.ALIYUN_STATUS)))
        {
            throw new ServiceException("短信通道未启用");
        }
        required(SmsConfigKeys.ALIYUN_ACCESS_KEY_ID, "阿里云短信 AccessKey");
        required(SmsConfigKeys.ALIYUN_ACCESS_KEY_SECRET, "阿里云短信 AccessKeySecret");
        required(SmsConfigKeys.ALIYUN_SIGN_NAME, "阿里云短信签名名称");
        required(SmsConfigKeys.ALIYUN_TEMPLATE_LOGIN, "登录模板编号");
    }

    private String value(String key)
    {
        return configService.selectConfigByKey(key);
    }

    private void required(String key, String name)
    {
        if (value(key) == null || value(key).isBlank())
        {
            throw new ServiceException(name + "不能为空");
        }
    }
}
