package com.zjude.web.controller.mini;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zjude.common.annotation.Anonymous;
import com.zjude.common.constant.Constants;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.domain.entity.SysUser;
import com.zjude.common.core.domain.model.LoginUser;
import com.zjude.common.utils.SecurityUtils;
import com.zjude.framework.web.service.TokenService;
import com.zjude.framework.web.service.UserDetailsServiceImpl;
import com.zjude.system.service.ISysUserService;

/** 微信小程序授权登录，服务端负责 code 换取 openid，不信任客户端传入的身份标识。 */
@RestController
@RequestMapping("/api/mini/auth")
@Anonymous
public class MiniAuthController
{
    private static final String SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Value("${app.wechat.app-id:}")
    private String appId;

    @Value("${app.wechat.app-secret:}")
    private String appSecret;

    @Resource
    private ISysUserService userService;

    @Resource
    private UserDetailsServiceImpl userDetailsService;

    @Resource
    private TokenService tokenService;

    @PostMapping("/login")
    public AjaxResult login(@RequestBody WechatLoginBody body)
    {
        if (body == null || !StringUtils.hasText(body.code()) || !StringUtils.hasText(body.nickname()))
        {
            return AjaxResult.error("微信授权信息不完整");
        }
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(appSecret))
        {
            return AjaxResult.error("微信登录未配置小程序 AppID 和密钥");
        }

        try
        {
            JSONObject session = exchangeCode(body.code());
            if (session.containsKey("errcode"))
            {
                return AjaxResult.error("微信授权校验失败，请重新授权");
            }
            String openId = session.getString("openid");
            if (!StringUtils.hasText(openId))
            {
                return AjaxResult.error("微信授权未返回用户标识");
            }

            String userName = "wx_" + DigestUtils.md5DigestAsHex(openId.getBytes(StandardCharsets.UTF_8));
            SysUser user = userService.selectUserByUserName(userName);
            if (user == null)
            {
                user = new SysUser();
                user.setUserName(userName);
                user.setNickName(body.nickname().trim());
                user.setAvatar(body.avatarUrl());
                user.setStatus("0");
                user.setPassword(SecurityUtils.encryptPassword(java.util.UUID.randomUUID().toString()));
                userService.insertUser(user);
            }
            else
            {
                SysUser update = new SysUser(user.getUserId());
                update.setNickName(body.nickname().trim());
                update.setAvatar(body.avatarUrl());
                userService.updateUserProfile(update);
                user.setNickName(update.getNickName());
                user.setAvatar(update.getAvatar());
            }

            LoginUser loginUser = (LoginUser) userDetailsService.createLoginUser(user);
            AjaxResult result = AjaxResult.success();
            result.put(Constants.TOKEN, tokenService.createToken(loginUser));
            return result;
        }
        catch (InterruptedException exception)
        {
            Thread.currentThread().interrupt();
            return AjaxResult.error("微信登录服务暂时不可用");
        }
        catch (Exception exception)
        {
            return AjaxResult.error("微信登录失败，请稍后重试");
        }
    }

    private JSONObject exchangeCode(String code) throws Exception
    {
        String query = "?appid=" + encode(appId) + "&secret=" + encode(appSecret) + "&js_code=" + encode(code) + "&grant_type=authorization_code";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SESSION_URL + query))
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return JSON.parseObject(response.body());
    }

    private String encode(String value)
    {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record WechatLoginBody(String code, String nickname, String avatarUrl)
    {
    }
}
