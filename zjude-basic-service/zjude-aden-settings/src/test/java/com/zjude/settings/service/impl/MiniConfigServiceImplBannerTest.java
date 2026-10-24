package com.zjude.settings.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.zjude.settings.domain.MiniBanner;
import com.zjude.system.domain.ZjudgBanner;
import com.zjude.system.service.ISysConfigService;
import com.zjude.system.service.IZjudgBannerService;

/**
 * 小程序轮播接口（zjudg_banner -> /api/mini/config/banners）的契约测试。
 */
@ExtendWith(MockitoExtension.class)
class MiniConfigServiceImplBannerTest
{
    @Mock
    private ISysConfigService configService;

    @Mock
    private IZjudgBannerService bannerService;

    @InjectMocks
    private MiniConfigServiceImpl service;

    @Test
    void mapsPublishedBannersToMiniContract()
    {
        ZjudgBanner internal = banner("B_1", "1", "/pages/services/index");
        ZjudgBanner none = banner("B_2", "0", "/pages/should-not-leak");
        when(bannerService.selectPublishedBanners("index")).thenReturn(List.of(internal, none));

        List<MiniBanner> result = service.banners("index");

        assertThat(result).hasSize(2);
        MiniBanner first = result.get(0);
        assertThat(first.getId()).isEqualTo("B_1");
        assertThat(first.getTitle()).isEqualTo("标题");
        assertThat(first.getSubtitle()).isEqualTo("备注");
        assertThat(first.getImage()).isEqualTo("/profile/banner/b1.jpg");
        assertThat(first.getLinkType()).isEqualTo("1");
        assertThat(first.getLink()).isEqualTo("/pages/services/index");
        // 无跳转的轮播不下发地址，前端不会误跳
        assertThat(result.get(1).getLink()).isEmpty();
        verify(bannerService).selectPublishedBanners("index");
    }

    @Test
    void returnsEmptyListWhenNothingPublished()
    {
        when(bannerService.selectPublishedBanners(null)).thenReturn(List.of());

        assertThat(service.banners(null)).isEmpty();
    }

    private ZjudgBanner banner(String id, String linkType, String linkUrl)
    {
        ZjudgBanner banner = new ZjudgBanner();
        banner.setBannerId(id);
        banner.setBannerTitle("标题");
        banner.setImageUrl("/profile/banner/b1.jpg");
        banner.setLinkType(linkType);
        banner.setLinkUrl(linkUrl);
        banner.setBannerPosition("index");
        banner.setBannerSort(1);
        banner.setStatus("0");
        banner.setStartTime(new Date(System.currentTimeMillis() - 60_000L));
        banner.setEndTime(new Date(System.currentTimeMillis() + 60_000L));
        banner.setRemark("备注");
        return banner;
    }
}
