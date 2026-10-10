package com.zjude.integrated.payment;

import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;
import com.zjude.system.domain.SysConfig;
import com.zjude.system.service.ISysConfigService;

@Service
public class DefaultPaymentConfigService implements PaymentConfigService
{
    @Resource
    private ISysConfigService configService;

    @Override
    public PaymentConfig get(PaymentProvider provider)
    {
        Map<String, String> values = loadValues();
        if (provider == PaymentProvider.WECHAT)
        {
            return new PaymentConfig(provider,
                    value(values, PaymentConfigKeys.WECHAT_APP_ID),
                    value(values, PaymentConfigKeys.WECHAT_MCH_ID),
                    value(values, PaymentConfigKeys.WECHAT_API_V3_KEY),
                    value(values, PaymentConfigKeys.WECHAT_CERTIFICATE),
                    value(values, PaymentConfigKeys.WECHAT_PRIVATE_KEY),
                    value(values, PaymentConfigKeys.WECHAT_CERTIFICATE_SERIAL_NO),
                    "",
                    "",
                    "",
                    value(values, PaymentConfigKeys.WECHAT_NOTIFY_URL),
                    "",
                    value(values, PaymentConfigKeys.WECHAT_AUTH_DIRECTORY),
                    "",
                    "1".equals(value(values, PaymentConfigKeys.WECHAT_ENABLED)));
        }
        if (provider == PaymentProvider.ALIPAY)
        {
            return new PaymentConfig(provider,
                    value(values, PaymentConfigKeys.ALIPAY_APP_ID),
                    "",
                    "",
                    "",
                    value(values, PaymentConfigKeys.ALIPAY_PRIVATE_KEY),
                    "",
                    value(values, PaymentConfigKeys.ALIPAY_GATEWAY, "https://openapi.alipay.com/gateway.do"),
                    value(values, PaymentConfigKeys.ALIPAY_SIGN_TYPE, "RSA2"),
                    value(values, PaymentConfigKeys.ALIPAY_CHARSET, "UTF-8"),
                    value(values, PaymentConfigKeys.ALIPAY_NOTIFY_URL),
                    value(values, PaymentConfigKeys.ALIPAY_RETURN_URL),
                    "",
                    value(values, PaymentConfigKeys.ALIPAY_PUBLIC_KEY),
                    "1".equals(value(values, PaymentConfigKeys.ALIPAY_ENABLED)));
        }
        throw new ServiceException("不支持的支付方式");
    }

    @Override
    public void validate(PaymentProvider provider)
    {
        PaymentConfig config = get(provider);
        if (provider == PaymentProvider.WECHAT)
        {
            require(config.appId(), "微信支付 AppID");
            require(config.mchId(), "微信支付商户号");
            require(config.apiV3Key(), "微信支付 APIv3 密钥");
            require(config.certificate(), "微信支付商户证书");
            require(config.privateKey(), "微信支付商户私钥");
            require(config.certificateSerialNo(), "微信支付证书序列号");
            require(config.notifyUrl(), "微信支付回调地址");
        }
        else
        {
            require(config.appId(), "支付宝应用 AppID");
            require(config.privateKey(), "支付宝应用私钥");
            require(config.publicKey(), "支付宝公钥");
            require(config.gateway(), "支付宝网关地址");
            require(config.notifyUrl(), "支付宝异步通知地址");
        }
    }

    private Map<String, String> loadValues()
    {
        Map<String, String> values = new HashMap<>();
        for (SysConfig config : configService.selectConfigList(new SysConfig()))
        {
            values.put(config.getConfigKey(), config.getConfigValue());
        }
        return values;
    }

    private String value(Map<String, String> values, String key)
    {
        return value(values, key, "");
    }

    private String value(Map<String, String> values, String key, String fallback)
    {
        return StringUtils.isEmpty(values.get(key)) ? fallback : values.get(key).trim();
    }

    private void require(String value, String name)
    {
        if (StringUtils.isEmpty(value))
        {
            throw new ServiceException(name + "不能为空");
        }
    }
}
