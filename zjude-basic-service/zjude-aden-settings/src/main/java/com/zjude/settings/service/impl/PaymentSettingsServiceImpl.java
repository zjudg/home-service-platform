package com.zjude.settings.service.impl;

import java.util.List;
import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.exception.ServiceException;
import com.zjude.integrated.payment.PaymentConfigService;
import com.zjude.integrated.payment.PaymentProvider;
import com.zjude.settings.service.IPageSettingsService;
import com.zjude.settings.service.PaymentSettingsService;
import com.zjude.system.domain.SysConfig;

@Service
public class PaymentSettingsServiceImpl implements PaymentSettingsService
{
    @Resource
    private IPageSettingsService pageSettingsService;

    @Resource
    private PaymentConfigService paymentConfigService;

    @Override
    public List<SysConfig> list()
    {
        return pageSettingsService.list("payment");
    }

    @Override
    public void save(Map<String, Object> settings, String username)
    {
        pageSettingsService.saveForm("payment", settings, username);
    }

    @Override
    public void validate(String provider)
    {
        paymentConfigService.validate(parseProvider(provider));
    }

    private PaymentProvider parseProvider(String provider)
    {
        if ("wechat".equalsIgnoreCase(provider))
        {
            return PaymentProvider.WECHAT;
        }
        if ("alipay".equalsIgnoreCase(provider))
        {
            return PaymentProvider.ALIPAY;
        }
        throw new ServiceException("不支持的支付方式：" + provider);
    }
}
