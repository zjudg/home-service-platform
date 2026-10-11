package com.zjude.integrated.sms;

import java.util.Collections;
import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.teaopenapi.models.Config;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;
import com.zjude.system.service.ISysConfigService;

@Service
public class AliyunSmsService
{
    @Resource
    private ISysConfigService configService;

    public void send(String mobile, SmsScene scene, Map<String, String> params)
    {
        requireMobile(mobile);
        String accessKeyId = required(SmsConfigKeys.ALIYUN_ACCESS_KEY_ID, "阿里云短信 AccessKey");
        String accessKeySecret = required(SmsConfigKeys.ALIYUN_ACCESS_KEY_SECRET, "阿里云短信 AccessKeySecret");
        String signName = required(signKey(), "阿里云短信签名");
        String templateCode = required(templateKey(scene), "阿里云短信模板编号");
        try
        {
            Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret)
                    .setEndpoint(value(SmsConfigKeys.ALIYUN_ENDPOINT, "dysmsapi.aliyuncs.com"));
            Client client = new Client(config);
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(mobile)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam(JSON.toJSONString(params == null ? Collections.emptyMap() : params));
            var response = client.sendSms(request);
            if (response == null || response.getBody() == null
                    || !"OK".equalsIgnoreCase(response.getBody().getCode()))
            {
                String message = response == null || response.getBody() == null
                        ? "阿里云短信服务无响应" : response.getBody().getMessage();
                throw new ServiceException("短信发送失败：" + message);
            }
        }
        catch (Exception exception)
        {
            if (exception instanceof ServiceException serviceException)
            {
                throw serviceException;
            }
            throw new ServiceException("短信发送失败：" + exception.getMessage());
        }
    }

    public void sendCode(String mobile, SmsScene scene, String code)
    {
        send(mobile, scene, Map.of("code", code));
    }

    private String signKey()
    {
        return StringUtils.isNotEmpty(value(SmsConfigKeys.ALIYUN_SIGN_NAME, ""))
                ? SmsConfigKeys.ALIYUN_SIGN_NAME : SmsConfigKeys.ALIYUN_SIGNATURE;
    }

    private String templateKey(SmsScene scene)
    {
        return switch (scene)
        {
            case LOGIN -> SmsConfigKeys.ALIYUN_TEMPLATE_LOGIN;
            case REGISTER -> SmsConfigKeys.ALIYUN_TEMPLATE_REGISTER;
            case RESET_PASSWORD -> SmsConfigKeys.ALIYUN_TEMPLATE_RESET_PASSWORD;
            case ORDER -> SmsConfigKeys.ALIYUN_TEMPLATE_ORDER;
        };
    }

    private String required(String key, String name)
    {
        String value = value(key, "");
        if (StringUtils.isEmpty(value))
        {
            throw new ServiceException(name + "不能为空");
        }
        return value;
    }

    private String value(String key, String fallback)
    {
        String value = configService.selectConfigByKey(key);
        return StringUtils.isEmpty(value) ? fallback : value.trim();
    }

    private void requireMobile(String mobile)
    {
        if (StringUtils.isEmpty(mobile) || !mobile.matches("^1\\d{10}$"))
        {
            throw new ServiceException("手机号格式不正确");
        }
    }
}
