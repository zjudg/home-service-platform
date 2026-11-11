package com.zjude.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 后台「用户管理」查询。
 *
 * <p>与系统内建的 {@code SysUserMapper.selectUserList} 分开：后者用于后台员工账号，
 * 带部门与角色数据权限；这里面向小程序注册用户，需要识别 openid 以区分注册来源。</p>
 */
public interface ZjudgUserQueryMapper
{
    /**
     * 按条件分页查询小程序用户。
     *
     * @param keyword 昵称 / 账号 / 手机号模糊匹配，可为空
     * @param beginTime 注册开始日期 yyyy-MM-dd，可为空
     * @param endTime 注册结束日期 yyyy-MM-dd，可为空
     * @return 用户列表，字段已按前端展示命名
     */
    List<Map<String, Object>> selectMiniUsers(@Param("keyword") String keyword,
            @Param("beginTime") String beginTime,
            @Param("endTime") String endTime);

    /**
     * 统计总数，与列表使用完全相同的筛选条件。
     *
     * @param keyword 关键字
     * @param beginTime 开始日期
     * @param endTime 结束日期
     * @return 总数
     */
    int countMiniUsers(@Param("keyword") String keyword,
            @Param("beginTime") String beginTime,
            @Param("endTime") String endTime);
}
