package com.zjude.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.zjude.common.core.domain.entity.SysUser;

/**
 * 用户�?数据�? * 
 * @author bai
 */
public interface SysUserMapper
{
    /**
     * 查询可接收订单通知的管理员账号（后台管理员及小程序业务管理员）。
     * 只返回启用且已绑定小程序 openid 的账号。
     *
     * @return 管理员账号
     */
    List<SysUser> selectOrderNotificationAdmins();
    /**
     * 根据条件分页查询用户列表
     * 
     * @param sysUser 用户信息
     * @return 用户信息集合信息
     */
    List<SysUser> selectUserList(SysUser sysUser);

    /**
     * 根据条件分页查询已配用户角色列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    List<SysUser> selectAllocatedList(SysUser user);

    /**
     * 根据条件分页查询未分配用户角色列�?     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    List<SysUser> selectUnallocatedList(SysUser user);

    /**
     * 通过用户名查询用�?     * 
     * @param userName 用户�?     * @return 用户对象信息
     */
    SysUser selectUserByUserName(String userName);

    /**
     * 通过手机号查询用户
     *
     * @param phoneNumber 手机号
     * @return 用户对象信息
     */
    SysUser selectUserByPhoneNumber(String phoneNumber);

    /**
     * 通过微信小程序 openid 查询用户，用于微信登录幂等与订阅消息定位用户。
     *
     * @param openid 微信 openid
     * @return 用户对象信息
     */
    SysUser selectUserByWxOpenid(String openid);

    /**
     * 补写用户的微信身份标识（openid / unionid）。
     *
     * @param user 至少包含 userId、wxOpenid
     * @return 影响行数
     */
    int updateUserWxIdentity(SysUser user);

    /**
     * 通过用户ID查询用户
     * 
     * @param userId 用户ID
     * @return 用户对象信息
     */
    SysUser selectUserById(String userId);

    /**
     * 新增用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    int insertUser(SysUser user);

    /**
     * 修改用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    int updateUser(SysUser user);

    /**
     * 修改用户头像
     * 
     * @param userName 用户�?     * @param avatar 头像地址
     * @return 结果
     */
    int updateUserAvatar(@Param("userName") String userName, @Param("avatar") String avatar);

    /**
     * 重置用户密码
     * 
     * @param userName 用户�?     * @param password 密码
     * @return 结果
     */
    int resetUserPwd(@Param("userName") String userName, @Param("password") String password);

    /**
     * 通过用户ID删除用户
     * 
     * @param userId 用户ID
     * @return 结果
     */
    int deleteUserById(String userId);

    /**
     * 批量删除用户信息
     * 
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    int deleteUserByIds(String[] userIds);

    /**
     * 校验用户名称是否唯一
     * 
     * @param userName 用户名称
     * @return 结果
     */
    SysUser checkUserNameUnique(String userName);

    /**
     * 校验手机号码是否唯一
     *
     * @param phoneNumber 手机号码
     * @return 结果
     */
    SysUser checkPhoneUnique(String phoneNumber);

    /**
     * 校验email是否唯一
     *
     * @param email 用户邮箱
     * @return 结果
     */
    SysUser checkEmailUnique(String email);
}
