package com.zjude.framework.web.service;

import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.zjude.common.constant.Constants;
import com.zjude.common.core.domain.entity.SysRole;
import com.zjude.common.core.domain.model.LoginUser;
import com.zjude.common.utils.SecurityUtils;
import com.zjude.common.utils.StringUtils;
import com.zjude.framework.security.context.PermissionContextHolder;

/**
 * bai首创 自定义权限实现，ss取自SpringSecurity首字�? * 
 * @author bai
 */
@Service("ss")
public class PermissionService
{
    /**
     * 验证用户是否具备某权�?     * 
     * @param permission 权限字符�?     * @return 用户是否具备某权�?     */
    public boolean hasPerm(String permission)
    {
        if (StringUtils.isEmpty(permission))
        {
            return false;
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getPermissions()))
        {
            return false;
        }
        PermissionContextHolder.setContext(permission);
        return hasPermissions(loginUser.getPermissions(), permission);
    }

    /**
     * 验证用户是否不具备某权限，与 hasPerm逻辑相反
     *
     * @param permission 权限字符�?     * @return 用户是否不具备某权限
     */
    public boolean lacksPerm(String permission)
    {
        return !hasPerm(permission);
    }

    /**
     * 验证用户是否具有以下任意一个权�?     *
     * @param permissions �?PERMISSION_DELIMETER 为分隔符的权限列�?     * @return 用户是否具有以下任意一个权�?     */
    public boolean hasAnyPerm(String permissions)
    {
        if (StringUtils.isEmpty(permissions))
        {
            return false;
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getPermissions()))
        {
            return false;
        }
        PermissionContextHolder.setContext(permissions);
        Set<String> authorities = loginUser.getPermissions();
        for (String permission : permissions.split(Constants.PERMISSION_DELIMETER))
        {
            if (permission != null && hasPermissions(authorities, permission))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断用户是否拥有某个角色
     * 
     * @param role 角色字符�?     * @return 用户是否具备某角�?     */
    public boolean hasRole(String role)
    {
        if (StringUtils.isEmpty(role))
        {
            return false;
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getUser().getRoles()))
        {
            return false;
        }
        for (SysRole sysRole : loginUser.getUser().getRoles())
        {
            String roleKey = sysRole.getRoleKey();
            if (Constants.SUPER_ADMIN.equals(roleKey) || roleKey.equals(StringUtils.trim(role)))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证用户是否不具备某角色，与 isRole逻辑相反�?     *
     * @param role 角色名称
     * @return 用户是否不具备某角色
     */
    public boolean lacksRole(String role)
    {
        return !hasRole(role);
    }

    /**
     * 验证用户是否具有以下任意一个角�?     *
     * @param roles �?ROLE_NAMES_DELIMETER 为分隔符的角色列�?     * @return 用户是否具有以下任意一个角�?     */
    public boolean hasAnyRoles(String roles)
    {
        if (StringUtils.isEmpty(roles))
        {
            return false;
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getUser().getRoles()))
        {
            return false;
        }
        for (String role : roles.split(Constants.ROLE_DELIMETER))
        {
            if (hasRole(role))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否包含权限
     * 
     * @param permissions 权限列表
     * @param permission 权限字符�?     * @return 用户是否具备某权�?     */
    private boolean hasPermissions(Set<String> permissions, String permission)
    {
        return permissions.contains(Constants.ALL_PERMISSION) || permissions.contains(StringUtils.trim(permission));
    }
}
