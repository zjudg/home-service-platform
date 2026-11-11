package com.zjude.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zjude.system.domain.ZjudgUserFavorite;

/**
 * 用户收藏数据访问。
 */
public interface ZjudgUserFavoriteMapper
{
    /**
     * 我的收藏列表，联表带出服务信息，按收藏时间倒序。
     *
     * @param userId 用户主键
     * @return 收藏列表
     */
    List<ZjudgUserFavorite> selectByUser(String userId);

    /**
     * 判断是否已收藏。
     *
     * @param userId 用户主键
     * @param serviceId 服务主键
     * @return 收藏记录，未收藏返回 null
     */
    ZjudgUserFavorite selectByUserAndService(@Param("userId") String userId, @Param("serviceId") String serviceId);

    int insert(ZjudgUserFavorite item);

    int deleteByUserAndService(@Param("userId") String userId, @Param("serviceId") String serviceId);

    int countByUser(String userId);

    /**
     * 后台按服务统计收藏数，用于服务运营分析。
     *
     * @param serviceId 服务主键
     * @return 收藏数
     */
    int countByService(@Param("serviceId") String serviceId);
}
