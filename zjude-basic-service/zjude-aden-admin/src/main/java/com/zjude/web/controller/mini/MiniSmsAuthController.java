package com.zjude.web.controller.mini;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zjude.common.annotation.Anonymous;
import com.zjude.common.annotation.RateLimiter;
import com.zjude.common.constant.CacheConstants;
import com.zjude.common.constant.Constants;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.domain.entity.SysUser;
import com.zjude.common.core.domain.model.LoginUser;
import com.zjude.common.core.redis.RedisCache;
import com.zjude.common.utils.MessageUtils;
import com.zjude.common.utils.SecurityUtils;
import com.zjude.framework.manager.AsyncManager;
import com.zjude.framework.manager.factory.AsyncFactory;
import com.zjude.framework.web.service.TokenService;
import com.zjude.framework.web.service.UserDetailsServiceImpl;
import com.zjude.integrated.sms.SmsConfigKeys;
import com.zjude.integrated.sms.SmsScene;
import com.zjude.integrated.sms.SmsService;
import com.zjude.system.service.ISysConfigService;
import com.zjude.system.service.ISysUserService;

/**
 * 小程序手机号验证码登录。
 *
 * <p>流程：先调用 /sms-code 下发验证码，再用 /sms-login 提交手机号与验证码。
 * 手机号首次登录会自动创建账号，注册与登录合并为一次调用。</p>
 */
@RestController
@RequestMapping("/api/mini/auth")
@Anonymous
public class MiniSmsAuthController
{
    /** 验证码有效期（分钟） */
    private static final long CODE_TTL_MINUTES = 5;

    /** 同一手机号发送间隔（秒） */
    private static final long SEND_INTERVAL_SECONDS = 60;

    private static final String PHONE_PATTERN = "^1\\d{10}$";

    private static final SecureRandom RANDOM = new SecureRandom();

    @Resource
    private RedisCache redisCache;

    @Resource
    private ISysConfigService configService;

    @Resource
    private SmsService smsService;

    @Resource
    private ISysUserService userService;

    @Resource
    private UserDetailsServiceImpl userDetailsService;

    @Resource
    private TokenService tokenService;

    /** 下发短信验证码 */
    // 按 IP 限流：同一 IP 每小时最多 10 次发送，防止短信接口被刷产生费用
    @RateLimiter(time = 3600, count = 10, limitType = com.zjude.common.enums.LimitType.IP)
    @PostMapping("/sms-code")
    public AjaxResult sendCode(@RequestBody SmsCodeBody body)
    {
        String phone = normalize(body == null ? null : body.phone());
        if (!phone.matches(PHONE_PATTERN))
        {
            return AjaxResult.error("请输入正确的手机号");
        }

        String intervalKey = CacheConstants.SMS_CODE_KEY + "interval:" + phone;
        if (redisCache.getCacheObject(intervalKey) != null)
        {
            return AjaxResult.error("验证码发送过于频繁，请稍后再试");
        }

        String cacheKey = CacheConstants.SMS_CODE_KEY + phone;
        String existing = redisCache.getCacheObject(cacheKey);
        if (!StringUtils.hasText(existing))
        {
            existing = String.format("%06d", RANDOM.nextInt(1_000_000));
            redisCache.setCacheObject(cacheKey, existing, (int) CODE_TTL_MINUTES, TimeUnit.MINUTES);
        }

        try
        {
            SmsScene scene = userService.selectUserByPhoneNumber(phone) == null ? SmsScene.REGISTER : SmsScene.LOGIN;
            smsService.sendCode(phone, scene, existing);
        }
        catch (Exception exception)
        {
            redisCache.deleteObject(cacheKey);
            return AjaxResult.error(exception.getMessage() == null ? "验证码发送失败" : exception.getMessage());
        }

        redisCache.setCacheObject(intervalKey, "1", (int) SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);
        AjaxResult result = AjaxResult.success("验证码已发送");
        result.put("expireIn", TimeUnit.MINUTES.toSeconds(CODE_TTL_MINUTES));
        result.put("resendIn", SEND_INTERVAL_SECONDS);
        if (isMockSmsEnabled())
        {
            // 未接通短信通道时回显验证码，便于联调；正式环境请关闭 sms.mock.enabled
            result.put("mockCode", existing);
        }
        return result;
    }

    private boolean isMockSmsEnabled()
    {
        try
        {
            return "true".equalsIgnoreCase(configService.selectConfigByKey(SmsConfigKeys.MOCK_ENABLED));
        }
        catch (Exception exception)
        {
            return false;
        }
    }

    /** 手机号 + 验证码登录，手机号未注册时自动注册并直接返回 token */
    // 按 IP 限流：同一 IP 每分钟最多 20 次登录尝试
    @RateLimiter(time = 60, count = 20, limitType = com.zjude.common.enums.LimitType.IP)
    @PostMapping("/sms-login")
    public AjaxResult login(@RequestBody SmsLoginBody body)
    {
        String phone = normalize(body == null ? null : body.phone());
        String code = body == null || body.code() == null ? "" : body.code().trim();
        if (!phone.matches(PHONE_PATTERN))
        {
            return AjaxResult.error("请输入正确的手机号");
        }
        if (!code.matches("^\\d{4,6}$"))
        {
            return AjaxResult.error("请输入正确的验证码");
        }

        String cacheKey = CacheConstants.SMS_CODE_KEY + phone;
        String cached = redisCache.getCacheObject(cacheKey);
        if (!StringUtils.hasText(cached))
        {
            return AjaxResult.error("验证码已失效，请重新获取");
        }
        if (!cached.equals(code))
        {
            return AjaxResult.error("验证码不正确");
        }
        redisCache.deleteObject(cacheKey);

        boolean registered = false;
        SysUser user = userService.selectUserByPhoneNumber(phone);
        if (user == null)
        {
            user = createUser(phone);
            registered = true;
        }

        LoginUser loginUser = (LoginUser) userDetailsService.createLoginUser(user);
        AsyncManager.me().execute(AsyncFactory.recordLoginInfo(user.getUserName(), Constants.LOGIN_SUCCESS,
                MessageUtils.message("user.login.success")));
        AjaxResult result = AjaxResult.success(registered ? "注册成功" : "登录成功");
        result.put(Constants.TOKEN, tokenService.createToken(loginUser));
        result.put("registered", registered);
        return result;
    }

    /** 手机号自动注册：账号名取手机号，随机密码避免空口令账号 */
    private SysUser createUser(String phone)
    {
        SysUser user = new SysUser();
        user.setUserName(phone);
        user.setNickName(maskPhone(phone));
        user.setPhoneNumber(phone);
        user.setStatus("0");
        user.setPassword(SecurityUtils.encryptPassword(java.util.UUID.randomUUID().toString()));
        userService.insertUser(user);
        return user;
    }

    private String normalize(String value)
    {
        return value == null ? "" : value.replaceAll("\\s", "");
    }

    private String maskPhone(String phone)
    {
        return phone.length() == 11 ? phone.substring(0, 3) + "****" + phone.substring(7) : phone;
    }

    public record SmsCodeBody(String phone)
    {
    }

    public record SmsLoginBody(String phone, String code)
    {
    }
}
