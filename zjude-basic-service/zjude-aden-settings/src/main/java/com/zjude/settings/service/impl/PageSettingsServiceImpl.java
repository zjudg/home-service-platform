package com.zjude.settings.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zjude.common.exception.ServiceException;
import com.zjude.integrated.payment.PaymentConfigKeys;
import com.zjude.integrated.sms.SmsConfigKeys;
import com.zjude.integrated.app.AppConfigKeys;
import com.zjude.integrated.map.MapConfigKeys;
import com.zjude.integrated.wechat.WechatConfigKeys;
import com.zjude.settings.service.IPageSettingsService;
import com.zjude.system.domain.SysConfig;
import com.zjude.system.service.ISysConfigService;

/**
 * 页面配置应用服务。
 *
 * 页面配置仍然落在 sys_config，但通过页面白名单隔离配置键，
 * 防止一个页面修改其他页面的配置。
 */
@Service
public class PageSettingsServiceImpl implements IPageSettingsService
{
    private static final Map<String, List<String>> PAGE_CONFIG_KEYS;

    static
    {
        Map<String, List<String>> pages = new LinkedHashMap<>();
        pages.put("website", prefixes("website."));
        pages.put("copyright", prefixes("filing."));
        pages.put("app", keys(
                AppConfigKeys.IOS_DOWNLOAD_URL,
                AppConfigKeys.ANDROID_DOWNLOAD_URL,
                AppConfigKeys.DOWNLOAD_TEXT,
                AppConfigKeys.PRIVACY_AGREEMENT,
                AppConfigKeys.WECHAT_LOGIN,
                WechatConfigKeys.APP_ID,
                WechatConfigKeys.APP_SECRET));
        pages.put("share", prefixes("share."));
        pages.put("notification", prefixes("notification."));
        pages.put("wechatSubscribe", keys(
                WechatConfigKeys.SUBSCRIBE_ENABLED,
                WechatConfigKeys.TEMPLATE_ACCEPTED,
                WechatConfigKeys.TEMPLATE_FINISHED,
                WechatConfigKeys.TEMPLATE_PAID,
                WechatConfigKeys.TEMPLATE_NEW_ORDER,
                WechatConfigKeys.TEMPLATE_ADMIN_CREATED,
                WechatConfigKeys.TEMPLATE_ADMIN_ACCEPTED,
                WechatConfigKeys.PAGE_ORDER,
                WechatConfigKeys.PAGE_MASTER_ORDER,
                WechatConfigKeys.PAGE_ADMIN_ORDER,
                WechatConfigKeys.FIELD_ACCEPTED,
                WechatConfigKeys.FIELD_FINISHED,
                WechatConfigKeys.FIELD_PAID,
                WechatConfigKeys.FIELD_NEW_ORDER,
                WechatConfigKeys.FIELD_ADMIN_CREATED,
                WechatConfigKeys.FIELD_ADMIN_ACCEPTED));
        pages.put("sms", keys(
                SmsConfigKeys.PROVIDER,
                SmsConfigKeys.ALIYUN_STATUS,
                SmsConfigKeys.ALIYUN_ACCESS_KEY_ID,
                SmsConfigKeys.ALIYUN_ACCESS_KEY_SECRET,
                SmsConfigKeys.ALIYUN_SIGNATURE,
                SmsConfigKeys.ALIYUN_SIGN_NAME,
                SmsConfigKeys.ALIYUN_ENDPOINT,
                SmsConfigKeys.ALIYUN_REGION_ID,
                SmsConfigKeys.ALIYUN_TEMPLATE_LOGIN,
                SmsConfigKeys.ALIYUN_TEMPLATE_REGISTER,
                SmsConfigKeys.ALIYUN_TEMPLATE_RESET_PASSWORD,
                SmsConfigKeys.ALIYUN_TEMPLATE_ORDER));
        pages.put("payment", keys(
                PaymentConfigKeys.WECHAT_ENABLED,
                PaymentConfigKeys.WECHAT_APP_ID,
                PaymentConfigKeys.WECHAT_MCH_ID,
                PaymentConfigKeys.WECHAT_API_V3_KEY,
                PaymentConfigKeys.WECHAT_CERTIFICATE,
                PaymentConfigKeys.WECHAT_PRIVATE_KEY,
                PaymentConfigKeys.WECHAT_CERTIFICATE_SERIAL_NO,
                PaymentConfigKeys.WECHAT_NOTIFY_URL,
                PaymentConfigKeys.WECHAT_AUTH_DIRECTORY,
                PaymentConfigKeys.WECHAT_PLATFORM_CERTIFICATE,
                PaymentConfigKeys.WECHAT_PLATFORM_SERIAL_NO,
                PaymentConfigKeys.WECHAT_PUBLIC_KEY_ID,
                PaymentConfigKeys.WECHAT_PUBLIC_KEY,
                PaymentConfigKeys.WECHAT_REFUND_NOTIFY_URL,
                PaymentConfigKeys.WECHAT_ORDER_DESCRIPTION_PREFIX,
                com.zjude.integrated.payment.TransferConfigKeys.AUTO_TRANSFER_ENABLED,
                com.zjude.integrated.payment.TransferConfigKeys.SCENE_ID,
                com.zjude.integrated.payment.TransferConfigKeys.SCENE_NAME,
                com.zjude.integrated.payment.TransferConfigKeys.REMARK,
                PaymentConfigKeys.ALIPAY_ENABLED,
                PaymentConfigKeys.ALIPAY_APP_ID,
                PaymentConfigKeys.ALIPAY_PRIVATE_KEY,
                PaymentConfigKeys.ALIPAY_PUBLIC_KEY,
                PaymentConfigKeys.ALIPAY_GATEWAY,
                PaymentConfigKeys.ALIPAY_SIGN_TYPE,
                PaymentConfigKeys.ALIPAY_CHARSET,
                PaymentConfigKeys.ALIPAY_NOTIFY_URL,
                PaymentConfigKeys.ALIPAY_RETURN_URL,
                "payment.balance.name", "payment.balance.icon", "payment.balance.sort", "payment.balance.status",
                "payment.wechat.name", "payment.wechat.icon", "payment.wechat.sort",
                "payment.alipay.name", "payment.alipay.icon", "payment.alipay.sort"));
        pages.put("map-tencent", keys(MapConfigKeys.TENCENT_KEY));
        pages.put("map-tianditu", keys(MapConfigKeys.TIANDITU_KEY));
        pages.put("channel-mini", prefixes("channel.mini."));
        pages.put("decorate-home", prefixes("decorate.home."));
        pages.put("decorate-profile", prefixes("decorate.profile."));
        pages.put("decorate-category", prefixes("decorate.category."));
        pages.put("decorate-tabbar", prefixes("decorate.tabbar."));
        PAGE_CONFIG_KEYS = Collections.unmodifiableMap(pages);
    }

    @Resource
    private ISysConfigService configService;

    @Override
    public List<SysConfig> list(String page)
    {
        List<String> rules = rules(page);
        return configService.selectConfigList(new SysConfig()).stream()
                .filter(config -> matches(config.getConfigKey(), rules))
                .peek(config -> {
                    if (isSecretKey(config.getConfigKey()))
                    {
                        config.setConfigValue("******");
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveForm(String page, Map<String, Object> settings, String username)
    {
        List<String> rules = rules(page);
        if (settings == null || settings.isEmpty())
        {
            throw new ServiceException("页面配置不能为空");
        }

        Map<String, SysConfig> records = new HashMap<>();
        for (SysConfig config : configService.selectConfigList(new SysConfig()))
        {
            if (matches(config.getConfigKey(), rules))
            {
                records.put(config.getConfigKey(), config);
            }
        }

        for (Map.Entry<String, Object> entry : settings.entrySet())
        {
            String key = entry.getKey();
            if (!matches(key, rules))
            {
                throw new ServiceException("配置不属于当前页面：" + key);
            }
            SysConfig config = records.get(key);
            String submitted = value(entry.getValue());
            if (isSecretKey(key) && (submitted.isBlank() || "******".equals(submitted)) && config != null)
            {
                continue;
            }
            if (config == null)
            {
                config = new SysConfig();
                config.setConfigName("页面配置-" + key);
                config.setConfigKey(key);
                config.setConfigType("N");
                config.setCreateBy(username);
                config.setRemark(page + "页面配置");
                config.setConfigValue(submitted);
                configService.insertConfig(config);
            }
            else
            {
                config.setConfigValue(submitted);
                config.setUpdateBy(username);
                configService.updateConfig(config);
            }
        }
        configService.resetConfigCache();
    }

    private String value(Object value)
    {
        return value == null ? "" : String.valueOf(value);
    }

    private boolean isSecretKey(String key)
    {
        String normalized = key == null ? "" : key.toLowerCase(Locale.ROOT);
        return PaymentConfigKeys.WECHAT_API_V3_KEY.equals(key)
                || PaymentConfigKeys.WECHAT_CERTIFICATE.equals(key)
                || PaymentConfigKeys.WECHAT_PRIVATE_KEY.equals(key)
                || PaymentConfigKeys.WECHAT_PUBLIC_KEY.equals(key)
                || PaymentConfigKeys.WECHAT_PLATFORM_CERTIFICATE.equals(key)
                || PaymentConfigKeys.ALIPAY_PRIVATE_KEY.equals(key)
                || PaymentConfigKeys.ALIPAY_PUBLIC_KEY.equals(key)
                || SmsConfigKeys.ALIYUN_ACCESS_KEY_SECRET.equals(key)
                || normalized.endsWith(".secret")
                || normalized.endsWith(".appsecret")
                || normalized.endsWith(".token")
                || normalized.endsWith(".encodingaeskey")
                || normalized.endsWith(".privatekey")
                || normalized.endsWith(".certificate");
    }

    private List<String> rules(String page)
    {
        List<String> rules = PAGE_CONFIG_KEYS.get(page);
        if (rules == null)
        {
            throw new ServiceException("不支持的页面配置：" + page);
        }
        return rules;
    }

    private boolean matches(String configKey, List<String> rules)
    {
        return configKey != null && rules.stream().anyMatch(rule ->
                rule.endsWith(".") ? configKey.startsWith(rule) : configKey.equals(rule));
    }

    private static List<String> prefixes(String... values)
    {
        return Arrays.asList(values);
    }

    private static List<String> keys(String... values)
    {
        return Arrays.asList(values);
    }
}
