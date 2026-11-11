package com.zjude.integrated.wechat;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zjude.common.constant.CacheConstants;
import com.zjude.common.core.redis.RedisCache;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;

/**
 * 微信小程序 access_token 获取与缓存。
 *
 * <p>access_token 全局唯一且有调用频率限制，必须集中缓存。微信返回的有效期为
 * 7200 秒，这里提前 300 秒过期，避免临界点拿到即将失效的令牌。</p>
 */
@Service
public class WechatAccessTokenService
{
    private static final Logger log = LoggerFactory.getLogger(WechatAccessTokenService.class);

    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/stable_token";

    /** 提前过期的秒数 */
    private static final long EXPIRE_AHEAD_SECONDS = 300;

    /** 单例锁：同一实例内避免并发重复刷新 */
    private final Object refreshLock = new Object();

    @Resource
    private RedisCache redisCache;

    @Resource
    private WechatConfigService wechatConfigService;

    /**
     * 获取可用的 access_token。
     *
     * <p>优先读缓存；缓存缺失时调用 stable_token 接口刷新。刷新失败直接抛异常，
     * 由上层决定是否降级（订阅消息失败不应阻断业务主流程）。</p>
     *
     * @return access_token
     */
    public String getAccessToken()
    {
        String cached = readCache();
        if (StringUtils.isNotEmpty(cached))
        {
            return cached;
        }
        synchronized (refreshLock)
        {
            // 双重检查：等锁期间可能有其他线程已完成刷新
            String recheck = readCache();
            if (StringUtils.isNotEmpty(recheck))
            {
                return recheck;
            }
            return refresh();
        }
    }

    /**
     * 强制刷新 access_token，用于令牌被判定失效后的重试。
     *
     * @return 新的 access_token
     */
    public String refresh()
    {
        if (!wechatConfigService.credentialsReady())
        {
            throw new ServiceException("微信小程序 AppID 与 AppSecret 未配置");
        }
        JSONObject body = new JSONObject();
        body.put("grant_type", "client_credential");
        body.put("appid", wechatConfigService.appId());
        body.put("secret", wechatConfigService.appSecret());
        body.put("force_refresh", true);

        try
        {
            HttpRequest request = HttpRequest.newBuilder(URI.create(TOKEN_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(body), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JSONObject result = JSON.parseObject(response.body());
            if (result == null)
            {
                throw new ServiceException("微信 access_token 返回为空");
            }
            Integer errcode = result.getInteger("errcode");
            if (errcode != null && errcode != 0)
            {
                // 不记录 secret，仅记录错误码与描述
                log.warn("微信 access_token 获取失败 errcode={} errmsg={}", errcode, result.getString("errmsg"));
                throw new ServiceException("微信 access_token 获取失败：" + result.getString("errmsg"));
            }
            String token = result.getString("access_token");
            if (StringUtils.isEmpty(token))
            {
                throw new ServiceException("微信 access_token 返回内容缺少 access_token");
            }
            Integer expiresIn = result.getInteger("expires_in");
            long ttl = (expiresIn == null ? 7200 : expiresIn) - EXPIRE_AHEAD_SECONDS;
            if (ttl <= 0)
            {
                ttl = 60;
            }
            redisCache.setCacheObject(cacheKey(), token, (int) ttl, TimeUnit.SECONDS);
            return token;
        }
        catch (ServiceException exception)
        {
            throw exception;
        }
        catch (InterruptedException exception)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("微信 access_token 获取被中断");
        }
        catch (Exception exception)
        {
            log.warn("微信 access_token 获取异常", exception);
            throw new ServiceException("微信 access_token 获取失败，请检查网络与小程序配置");
        }
    }

    /**
     * 清除缓存，用于令牌失效后强制重新获取。
     */
    public void evict()
    {
        redisCache.deleteObject(cacheKey());
    }

    private String readCache()
    {
        try
        {
            Object value = redisCache.getCacheObject(cacheKey());
            return value == null ? null : String.valueOf(value);
        }
        catch (Exception exception)
        {
            // 缓存不可用时降级为直接刷新，不影响主流程
            return null;
        }
    }

    private String cacheKey()
    {
        String appId = wechatConfigService.appId();
        return CacheConstants.WECHAT_ACCESS_TOKEN_KEY + (StringUtils.isEmpty(appId) ? "default" : appId);
    }

    /**
     * 拼接带查询参数的 URL，供其他微信接口复用。
     *
     * @param base 基础地址
     * @param key 参数名
     * @param value 参数值
     * @return 完整地址
     */
    static String withQuery(String base, String key, String value)
    {
        return base + "?" + key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
