package com.zjude.system.service;

import java.util.Map;

/**
 * 后台用户管理查询服务。
 */
public interface IZjudgUserQueryService
{
    /**
     * 分页查询小程序注册用户。
     *
     * @param keyword 昵称 / 账号 / 手机号模糊匹配
     * @param beginTime 注册开始日期 yyyy-MM-dd
     * @param endTime 注册结束日期 yyyy-MM-dd
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @return total 与 rows 两个键，rows 内字段已按前端展示命名
     */
    Map<String, Object> pageMiniUsers(String keyword, String beginTime, String endTime, int pageNum, int pageSize);

    /**
     * 设置小程序业务管理员角色，仅允许绑定到已注册的小程序用户。
     */
    void setMiniAdmin(String userId, boolean enabled);
}
