package com.zjude.framework.security.handle;

import com.zjude.common.exception.base.BaseException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import com.alibaba.fastjson2.JSON;
import com.zjude.common.constant.Constants;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.domain.model.LoginUser;
import com.zjude.common.utils.MessageUtils;
import com.zjude.common.utils.ServletUtils;
import com.zjude.common.utils.StringUtils;
import com.zjude.framework.manager.AsyncManager;
import com.zjude.framework.manager.factory.AsyncFactory;
import com.zjude.framework.web.service.TokenService;

/**
 * 自定义退出处理类 返回成功
 * 
 * @author bai
 */
@Configuration
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler
{
    @Resource
    private TokenService tokenService;

    /**
     * 退出处�?     *
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws BaseException
    {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser))
        {
            String userName = loginUser.getUsername();
            // 删除用户缓存记录
            tokenService.delLoginUser(loginUser.getToken());
            // 记录用户退出日志
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(userName, Constants.LOGOUT, MessageUtils.message("user.logout.success")));
        }
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.success(MessageUtils.message("user.logout.success"))));
    }
}
