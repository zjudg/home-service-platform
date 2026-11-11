package com.zjude.integrated.wechat;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.utils.StringUtils;
import com.zjude.system.service.ISysConfigService;

/**
 * 微信小程序配置读取。
 *
 * <p>配置统一来自 {@code sys_config}，后台修改后即时生效，无需重启。
 * 读取失败或未配置时返回空串而不是抛异常，由调用方决定降级策略。</p>
 */
@Service
public class WechatConfigService
{
    @Resource
    private ISysConfigService configService;

    /**
     * 读取配置值，键不存在时返回空串。
     *
     * @param key 配置键
     * @return 配置值
     */
    public String get(String key)
    {
        try
        {
            String value = configService.selectConfigByKey(key);
            return value == null ? "" : value.trim();
        }
        catch (Exception exception)
        {
            return "";
        }
    }

    /** 是否开启小程序登录 */
    public boolean loginEnabled()
    {
        return "1".equals(get(WechatConfigKeys.LOGIN_ENABLED));
    }

    /** 小程序 AppID */
    public String appId()
    {
        return get(WechatConfigKeys.APP_ID);
    }

    /** 小程序 AppSecret */
    public String appSecret()
    {
        return get(WechatConfigKeys.APP_SECRET);
    }

    /** AppID 与 AppSecret 是否都已配置 */
    public boolean credentialsReady()
    {
        return StringUtils.isNotEmpty(appId()) && StringUtils.isNotEmpty(appSecret());
    }

    /** 是否开启订阅消息 */
    public boolean subscribeEnabled()
    {
        return "1".equals(get(WechatConfigKeys.SUBSCRIBE_ENABLED));
    }

    /**
     * 读取订阅消息模板 ID。
     *
     * @param key 模板配置键，取自 {@link WechatConfigKeys}
     * @return 模板 ID，未配置时为空串
     */
    public String template(String key)
    {
        return get(key);
    }
}
