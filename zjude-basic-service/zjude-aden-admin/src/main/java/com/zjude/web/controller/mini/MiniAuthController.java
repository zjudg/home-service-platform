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
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zjude.common.annotation.Anonymous;
import com.zjude.common.constant.Constants;
import com.zjude.common.utils.MessageUtils;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.domain.entity.SysUser;
import com.zjude.common.core.domain.model.LoginUser;
import com.zjude.common.utils.SecurityUtils;
import com.zjude.framework.manager.AsyncManager;
import com.zjude.framework.manager.factory.AsyncFactory;
import com.zjude.framework.web.service.TokenService;
import com.zjude.framework.web.service.UserDetailsServiceImpl;
import com.zjude.integrated.wechat.WechatConfigService;
import com.zjude.system.mapper.SysUserMapper;
import com.zjude.system.service.ISysUserService;

/** 微信小程序授权登录，服务端负责 code 换取 openid，不信任客户端传入的身份标识。 */
@RestController
@RequestMapping("/api/mini/auth")
@Anonymous
public class MiniAuthController
{
    private static final String SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Resource
    private WechatConfigService wechatConfigService;

    @Resource
    private SysUserMapper userMapper;

    @Resource
    private ISysUserService userService;

    @Resource
    private UserDetailsServiceImpl userDetailsService;

    @Resource
    private TokenService tokenService;

    @PostMapping("/login")
    public AjaxResult login(@RequestBody WechatLoginBody body)
    {
        if (body == null || !StringUtils.hasText(body.code())
                || (!body.silent() && !StringUtils.hasText(body.nickname())))
        {
            return AjaxResult.error("微信授权信息不完整");
        }
        if (!wechatConfigService.loginEnabled())
        {
            return AjaxResult.error("微信登录未启用，请联系管理员在后台开启");
        }
        String appId = wechatConfigService.appId();
        String appSecret = wechatConfigService.appSecret();
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(appSecret))
        {
            return AjaxResult.error("微信登录未配置小程序 AppID 和密钥");
        }

        try
        {
            JSONObject session = exchangeCode(body.code(), appId, appSecret);
            if (session.containsKey("errcode"))
            {
                return AjaxResult.error("微信授权校验失败，请重新授权");
            }
            String openId = session.getString("openid");
            if (!StringUtils.hasText(openId))
            {
                return AjaxResult.error("微信授权未返回用户标识");
            }
            String unionId = session.getString("unionid");

            // 优先按 openid 命中，兼容历史仅按 md5 用户名建号的账号
            SysUser user = userService.selectUserByWxOpenid(openId);
            if (user == null)
            {
                String userName = "wx_" + DigestUtils.md5DigestAsHex(openId.getBytes(StandardCharsets.UTF_8));
                user = userService.selectUserByUserName(userName);
                if (user != null)
                {
                    // 历史账号补写 openid，使其可接收订阅消息
                    SysUser patch = new SysUser(user.getUserId());
                    patch.setWxOpenid(openId);
                    patch.setWxUnionid(unionId);
                    userMapper.updateUserWxIdentity(patch);
                    user.setWxOpenid(openId);
                    user.setWxUnionid(unionId);
                }
            }
            if (user == null)
            {
                // 静默登录只负责识别已注册用户；新用户必须回到登录页完成资料授权。
                if (body.silent())
                {
                    return AjaxResult.success("用户尚未注册").put("registered", false);
                }
                user = new SysUser();
                user.setUserName("wx_" + DigestUtils.md5DigestAsHex(openId.getBytes(StandardCharsets.UTF_8)));
                user.setWxOpenid(openId);
                user.setWxUnionid(unionId);
                user.setNickName(StringUtils.hasText(body.nickname()) ? body.nickname().trim() : "微信用户");
                user.setAvatar(body.avatarUrl());
                user.setStatus("0");
                user.setPassword(SecurityUtils.encryptPassword(java.util.UUID.randomUUID().toString()));
                userService.insertUser(user);
            }
            else
            {
                if (!body.silent() && (StringUtils.hasText(body.nickname()) || StringUtils.hasText(body.avatarUrl())))
                {
                    SysUser update = new SysUser(user.getUserId());
                    if (StringUtils.hasText(body.nickname())) update.setNickName(body.nickname().trim());
                    if (StringUtils.hasText(body.avatarUrl())) update.setAvatar(body.avatarUrl());
                    userService.updateUserProfile(update);
                    if (StringUtils.hasText(update.getNickName())) user.setNickName(update.getNickName());
                    if (StringUtils.hasText(update.getAvatar())) user.setAvatar(update.getAvatar());
                }
            }

            LoginUser loginUser = (LoginUser) userDetailsService.createLoginUser(user);
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(user.getUserName(), Constants.LOGIN_SUCCESS,
                    MessageUtils.message("user.login.success")));
            AjaxResult result = AjaxResult.success();
            result.put(Constants.TOKEN, tokenService.createToken(loginUser));
            result.put("registered", true);
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

    private JSONObject exchangeCode(String code, String appId, String appSecret) throws Exception
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

    public record WechatLoginBody(String code, String nickname, String avatarUrl, boolean silent)
    {
    }
}
