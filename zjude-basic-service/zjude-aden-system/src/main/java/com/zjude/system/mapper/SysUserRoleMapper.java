package com.zjude.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zjude.system.domain.SysUserRole;

/**
 * 用户与角色关联表 数据�? * 
 * @author bai
 */
public interface SysUserRoleMapper
{
    /**
     * 通过用户ID删除用户和角色关�?     * 
     * @param userId 用户ID
     * @return 结果
     */
    int deleteUserRoleByUserId(String userId);

    /**
     * 批量删除用户和角色关�?     * 
     * @param userIds 需要删除的数据ID
     * @return 结果
     */
    int deleteUserRole(String[] userIds);

    /**
     * 通过角色ID查询角色使用数量
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    int countUserRoleByRoleId(String roleId);

    /**
     * 批量新增用户角色信息
     * 
     * @param userRoleList 用户角色列表
     * @return 结果
     */
    int batchUserRole(List<SysUserRole> userRoleList);

    /**
     * 删除用户和角色关联信�?     * 
     * @param userRole 用户和角色关联信�?     * @return 结果
     */
    int deleteUserRoleInfo(SysUserRole userRole);

    /**
     * 为小程序用户授予业务管理员角色；SQL 使用 NOT EXISTS 保证重复点击幂等。
     */
    int grantMiniAdmin(@Param("userId") String userId);

    /**
     * 撤销小程序用户的业务管理员角色。
     */
    int revokeMiniAdmin(@Param("userId") String userId);

    /**
     * 批量取消授权用户角色
     * 
     * @param roleId 角色ID
     * @param userIds 需要删除的用户数据ID
     * @return 结果
     */
    int deleteUserRoleInfos(@Param("roleId") String roleId, @Param("userIds") String[] userIds);
}
