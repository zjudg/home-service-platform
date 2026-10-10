package com.zjude.integrated.sms;

import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;
import com.zjude.system.service.ISysConfigService;

@Service
public class SmsServiceFacade implements SmsService
{
    @Resource
    private ISysConfigService configService;

    @Resource
    private AliyunSmsService aliyunSmsService;

    @Override
    public void send(String mobile, SmsScene scene, Map<String, String> params)
    {
        if (!"1".equals(configService.selectConfigByKey(SmsConfigKeys.ALIYUN_STATUS)))
        {
            throw new ServiceException("短信通道未启用");
        }
        aliyunSmsService.send(mobile, scene, params);
    }

    @Override
    public void sendCode(String mobile, SmsScene scene, String code)
    {
        if (StringUtils.isEmpty(code))
        {
            throw new ServiceException("短信验证码不能为空");
        }
        send(mobile, scene, Map.of("code", code));
    }
}
