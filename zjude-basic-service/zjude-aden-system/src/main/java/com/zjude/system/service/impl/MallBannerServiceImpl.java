package com.zjude.system.service.impl;

import java.util.List;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.enums.BannerLinkType;
import com.zjude.common.enums.BannerPosition;
import com.zjude.common.enums.VisibleStatus;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.MallBanner;
import com.zjude.system.mapper.MallBannerMapper;
import com.zjude.system.service.IMallBannerService;

/**
 * 轮播图 服务层处理
 *
 * @author bai
 */
@Service("mallBannerService")
public class MallBannerServiceImpl implements IMallBannerService
{
    @Resource
    private MallBannerMapper bannerMapper;

    @Resource
    private BaseKeyGenerate keyGenerate;

    @Override
    public List<MallBanner> selectBannerList(MallBanner banner)
    {
        return bannerMapper.selectBannerList(banner);
    }

    @Override
    public MallBanner selectBannerById(String bannerId)
    {
        return bannerMapper.selectBannerById(bannerId);
    }

    /**
     * 新增轮播图，生成字符串业务主键并校验业务不变量
     *
     * @param banner 轮播图信息
     * @return 结果
     */
    @Override
    public int insertBanner(MallBanner banner)
    {
        checkBannerAllowed(banner);
        banner.setBannerId(keyGenerate.snowflakeStringKey("B_"));
        return bannerMapper.insertBanner(banner);
    }

    /**
     * 修改轮播图，校验业务不变量
     *
     * @param banner 轮播图信息
     * @return 结果
     */
    @Override
    public int updateBanner(MallBanner banner)
    {
        checkBannerAllowed(banner);
        return bannerMapper.updateBanner(banner);
    }

    @Override
    public int deleteBannerById(String bannerId)
    {
        return bannerMapper.deleteBannerById(bannerId);
    }

    @Override
    public int deleteBannerByIds(String[] bannerIds)
    {
        return bannerMapper.deleteBannerByIds(bannerIds);
    }

    /**
     * 校验轮播图业务不变量，取值必须来自枚举字典，跳转与生效时间必须自洽
     *
     * @param banner 轮播图信息
     */
    private void checkBannerAllowed(MallBanner banner)
    {
        if (!BannerLinkType.isValid(banner.getLinkType()))
        {
            throw new ServiceException("跳转类型取值不合法");
        }
        if (!BannerPosition.isValid(banner.getBannerPosition()))
        {
            throw new ServiceException("广告位置取值不合法");
        }
        if (!VisibleStatus.isValid(banner.getStatus()))
        {
            throw new ServiceException("状态取值不合法");
        }
        if (!BannerLinkType.NONE.getCode().equals(banner.getLinkType()) && StringUtils.isBlank(banner.getLinkUrl()))
        {
            throw new ServiceException("跳转类型为内部页面或外部链接时，跳转地址不能为空");
        }
        if (banner.getStartTime() != null && banner.getEndTime() != null
                && !banner.getStartTime().before(banner.getEndTime()))
        {
            throw new ServiceException("生效开始时间必须早于生效结束时间");
        }
    }
}
