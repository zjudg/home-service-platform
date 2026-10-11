package com.zjude.system.service;

import java.util.List;
import com.zjude.system.domain.MallBanner;

/**
 * 轮播图 服务层
 *
 * @author bai
 */
public interface IMallBannerService
{
    /**
     * 查询轮播图集合
     *
     * @param banner 轮播图信息
     * @return 轮播图集合
     */
    List<MallBanner> selectBannerList(MallBanner banner);

    /**
     * 根据轮播图ID查询信息
     *
     * @param bannerId 轮播图ID
     * @return 轮播图信息
     */
    MallBanner selectBannerById(String bannerId);

    /**
     * 新增轮播图
     *
     * @param banner 轮播图信息
     * @return 结果
     */
    int insertBanner(MallBanner banner);

    /**
     * 修改轮播图
     *
     * @param banner 轮播图信息
     * @return 结果
     */
    int updateBanner(MallBanner banner);

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
