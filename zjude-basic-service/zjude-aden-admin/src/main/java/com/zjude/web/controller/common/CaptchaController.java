package com.zjude.web.controller.common;

import java.util.concurrent.TimeUnit;

import com.zjude.common.exception.base.BaseException;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.ChineseCaptcha;
import com.wf.captcha.base.Captcha;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.config.BaiConfig;
import com.zjude.common.constant.CacheConstants;
import com.zjude.common.constant.Constants;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.redis.RedisCache;
import com.zjude.common.utils.uuid.IdUtils;
import com.zjude.system.service.ISysConfigService;

/**
 * 验证码操作处�? * 
 * @author bai
 */
@RestController
public class CaptchaController
{
    @Resource
    private RedisCache redisCache;

    @Resource
    private ISysConfigService configService;
    /**
     * 生成验证�?     */
    @GetMapping("/captcha/image")
    public AjaxResult getCode() throws BaseException
    {
        AjaxResult ajax = AjaxResult.success();
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        ajax.put("captchaEnabled", captchaEnabled);
        if (!captchaEnabled)
        {
            return ajax;
        }

        // 保存验证码信息
        String uuid = IdUtils.simpleUUID();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;

        String code = null;

        // 生成验证码
        String captchaType = BaiConfig.getCaptchaType();
        Captcha captcha = null;
        if ("math".equals(captchaType))
        {
            captcha = new ArithmeticCaptcha(115, 42);
            code = captcha.text(); //得到算术验证码的计算�?/创建算术验证�?
        }
        else if ("char".equals(captchaType))
        {
            captcha = new ChineseCaptcha(115,42); //创建
            code = captcha.text();
        }

        redisCache.setCacheObject(verifyKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        ajax.put("uuid", uuid);
        if (captcha != null) {
            ajax.put("img", captcha.toBase64());
        }
        return ajax;
    }
}
