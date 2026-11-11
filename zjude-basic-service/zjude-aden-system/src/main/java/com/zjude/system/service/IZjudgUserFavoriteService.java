package com.zjude.system.service;

import java.util.List;
import com.zjude.system.domain.ZjudgUserFavorite;

/**
 * 用户收藏应用服务。
 */
public interface IZjudgUserFavoriteService
{
    /**
     * 我的收藏。
     *
     * @param userId 用户主键
     * @return 收藏列表，含服务快照字段
     */
    List<ZjudgUserFavorite> list(String userId);

    /**
     * 当前用户是否收藏了该服务。
     *
     * @param userId 用户主键
     * @param serviceId 服务主键
     * @return 是否已收藏
     */
    boolean isFavorite(String userId, String serviceId);

    /**
     * 收藏服务，已收藏时幂等返回。
     *
     * @param userId 用户主键
     * @param serviceId 服务主键
     */
    void add(String userId, String serviceId);

    /**
     * 取消收藏，未收藏时幂等返回。
     *
     * @param userId 用户主键
     * @param serviceId 服务主键
     */
    void remove(String userId, String serviceId);

    /**
     * 收藏数量，用于「我的」页角标。
     *
     * @param userId 用户主键
     * @return 收藏数
     */
    int count(String userId);
}
