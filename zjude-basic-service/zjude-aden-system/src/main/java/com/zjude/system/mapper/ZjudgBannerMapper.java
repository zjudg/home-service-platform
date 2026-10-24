package com.zjude.system.mapper;

import java.util.List;
import com.zjude.system.domain.ZjudgBanner;

/**
 * 轮播图 数据层
 *
 * @author bai
 */
public interface ZjudgBannerMapper
{
    /**
     * 查询轮播图集合
     *
     * @param banner 轮播图信息
     * @return 轮播图集合
     */
    List<ZjudgBanner> selectBannerList(ZjudgBanner banner);

    /**
     * 查询指定广告位置下当前生效的轮播图，仅返回启用且在有效期内的数据。
     *
     * @param bannerPosition 广告位置，如 index
     * @return 轮播图集合，按显示顺序升序
     */
    List<ZjudgBanner> selectPublishedByPosition(String bannerPosition);

    /**
     * 根据轮播图ID查询信息
     *
     * @param bannerId 轮播图ID
     * @return 轮播图信息
     */
    ZjudgBanner selectBannerById(String bannerId);

    /**
     * 新增轮播图
     *
     * @param banner 轮播图信息
     * @return 结果
     */
    int insertBanner(ZjudgBanner banner);

    /**
     * 修改轮播图
     *
     * @param banner 轮播图信息
     * @return 结果
     */
    int updateBanner(ZjudgBanner banner);

    /**
     * 删除轮播图
     *
     * @param bannerId 轮播图ID
     * @return 结果
     */
    int deleteBannerById(String bannerId);

    /**
     * 批量删除轮播图
     *
     * @param bannerIds 需要删除的轮播图ID数组
     * @return 结果
     */
    int deleteBannerByIds(String[] bannerIds);
}
