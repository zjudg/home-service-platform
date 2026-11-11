package com.zjude.integrated.wechat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;

/**
 * 微信小程序订阅消息发送。
 *
 * <p>设计原则：推送属于通知类副作用，<b>任何失败都不应阻断下单、接单、完工等主流程</b>。
 * 因此本服务不向外抛异常，统一返回 {@link SendResult} 并记录日志，由调用方决定是否关心结果。</p>
 *
 * <p>微信订阅消息为一次性授权：每条消息消耗用户一次授权额度。额度不足时微信返回
 * 43101，这里按「跳过」处理，不视为错误。</p>
 */
@Service
public class WechatSubscribeMessageService
{
    private static final Logger log = LoggerFactory.getLogger(WechatSubscribeMessageService.class);

    private static final String SEND_URL = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send";

    /** 用户拒绝接收 / 授权额度已用尽 */
    private static final int ERRCODE_NO_QUOTA = 43101;

    /** access_token 失效 */
    private static final int ERRCODE_TOKEN_INVALID = 40001;

    /** access_token 过期 */
    private static final int ERRCODE_TOKEN_EXPIRED = 42001;

    @Resource
    private WechatAccessTokenService accessTokenService;

    @Resource
    private WechatConfigService wechatConfigService;

    /**
     * 发送订阅消息。
     *
     * @param openid 接收用户 openid
     * @param templateId 订阅消息模板 ID
     * @param page 点击通知后跳转的小程序页面，可为空
     * @param data 模板数据，键为模板字段名，值为实际内容
     * @return 发送结果，永不返回 null
     */
    public SendResult send(String openid, String templateId, String page, Map<String, String> data)
    {
        if (!wechatConfigService.subscribeEnabled())
        {
            return SendResult.skipped("订阅消息未启用");
        }
        if (StringUtils.isEmpty(templateId))
        {
            return SendResult.skipped("模板 ID 未配置");
        }
        if (StringUtils.isEmpty(openid))
        {
            return SendResult.skipped("用户未绑定微信 openid");
        }
        if (!wechatConfigService.credentialsReady())
        {
            return SendResult.skipped("小程序 AppID 与 AppSecret 未配置");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("touser", openid);
        body.put("template_id", templateId);
        if (StringUtils.isNotEmpty(page))
        {
            body.put("page", page);
        }
        body.put("data", formatData(data));
        body.put("miniprogram_state", "formal");
        body.put("lang", "zh_CN");

        try
        {
            SendResult result = doSend(body, false);
            if (result.tokenExpired())
            {
                // 令牌失效时刷新并重试一次
                accessTokenService.evict();
                result = doSend(body, true);
            }
            return result;
        }
        catch (Exception exception)
        {
            log.warn("订阅消息发送异常 openid={} template={}", mask(openid), templateId, exception);
            return SendResult.failed("发送异常：" + exception.getMessage());
        }
    }

    /**
     * 实际调用发送接口。
     *
     * @param body 请求体
     * @param refreshed 是否已刷新过令牌，用于避免无限重试
     * @return 发送结果
     * @throws Exception 网络异常
     */
    private SendResult doSend(Map<String, Object> body, boolean refreshed) throws Exception
    {
        String token = accessTokenService.getAccessToken();
        String url = SEND_URL + "?access_token=" + token;
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(body), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JSONObject result = JSON.parseObject(response.body());
        if (result == null)
        {
            return SendResult.failed("微信返回为空");
        }
        Integer errcode = result.getInteger("errcode");
        if (errcode == null || errcode == 0)
        {
            return SendResult.success();
        }
        String errmsg = result.getString("errmsg");
        if (errcode == ERRCODE_NO_QUOTA)
        {
            // 用户未授权或额度用尽，属于正常业务情况，不算失败
            log.info("订阅消息跳过：用户无可用授权额度 errcode={} template={}", errcode, body.get("template_id"));
            return SendResult.skipped("用户无可用订阅授权");
        }
        if ((errcode == ERRCODE_TOKEN_INVALID || errcode == ERRCODE_TOKEN_EXPIRED) && !refreshed)
        {
            log.info("订阅消息令牌失效，准备刷新后重试 errcode={}", errcode);
            return SendResult.expired();
        }
        log.warn("订阅消息发送失败 errcode={} errmsg={} template={}", errcode, errmsg, body.get("template_id"));
        return SendResult.failed(errmsg);
    }

    /**
     * 转换为微信要求的 {key: {value: xxx}} 结构。
     *
     * @param data 原始数据
     * @return 模板数据
     */
    private Map<String, Object> formatData(Map<String, String> data)
    {
        Map<String, Object> formatted = new LinkedHashMap<>();
        if (data == null)
        {
            return formatted;
        }
        data.forEach((key, value) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            // 微信要求字段值不超过 20 个字符，超出会直接报错
            String text = value == null ? "" : value;
            if (text.length() > 20)
            {
                text = text.substring(0, 20);
            }
            item.put("value", text);
            formatted.put(key, item);
        });
        return formatted;
    }

    /**
     * openid 脱敏，避免日志泄露用户标识。
     *
     * @param openid 原始 openid
     * @return 脱敏值
     */
    private String mask(String openid)
    {
        if (openid == null || openid.length() <= 8)
        {
            return "***";
        }
        return openid.substring(0, 4) + "***" + openid.substring(openid.length() - 4);
    }

    /**
     * 订阅消息发送结果。
     *
     * @param status 状态
     * @param message 说明
     */
    public record SendResult(Status status, String message)
    {
        /** 结果状态 */
        public enum Status
        {
            /** 发送成功 */
            SUCCESS,
            /** 主动跳过（未启用、未配置、无授权额度） */
            SKIPPED,
            /** 发送失败 */
            FAILED,
            /** 令牌失效，需要重试 */
            EXPIRED
        }

        static SendResult success()
        {
            return new SendResult(Status.SUCCESS, null);
        }

        static SendResult skipped(String message)
        {
            return new SendResult(Status.SKIPPED, message);
        }

        static SendResult failed(String message)
        {
            return new SendResult(Status.FAILED, message);
        }

        static SendResult expired()
        {
            return new SendResult(Status.EXPIRED, null);
        }

        boolean tokenExpired()
        {
            return status == Status.EXPIRED;
        }

        /** 是否真正发出 */
        public boolean sent()
        {
            return status == Status.SUCCESS;
        }
    }
}
