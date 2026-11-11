package com.zjude.web.controller.mini;

import com.zjude.common.annotation.Log;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.domain.model.LoginUser;
import com.zjude.common.enums.BusinessType;
import com.zjude.framework.web.service.TokenService;
import com.zjude.system.service.ISysUserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Authenticated self-service operations for a mini-program user's profile. */
@RestController
@Validated
@RequestMapping("/api/mini/user/profile")
public class MiniUserProfileController extends BaseController
{
    @Resource private ISysUserService userService;
    @Resource private TokenService tokenService;

    /** Persist the OSS URL returned by the shared upload service as the current user's avatar. */
    @Log(title = "小程序用户头像", businessType = BusinessType.UPDATE)
    @PutMapping("/avatar")
    public AjaxResult updateAvatar(@Valid @RequestBody AvatarBody body)
    {
        LoginUser loginUser = getLoginUser();
        if (!userService.updateUserAvatar(loginUser.getUsername(), body.avatarUrl()))
        {
            return error("头像保存失败，请稍后重试");
        }

        loginUser.getUser().setAvatar(body.avatarUrl());
        tokenService.setLoginUser(loginUser);
        return success();
    }

    public record AvatarBody(
            @NotBlank(message = "头像地址不能为空")
            @Size(max = 500, message = "头像地址过长")
            @Pattern(regexp = "^https?://\\S+$", message = "头像必须使用上传后返回的 OSS 地址")
            String avatarUrl)
    {
    }
}
