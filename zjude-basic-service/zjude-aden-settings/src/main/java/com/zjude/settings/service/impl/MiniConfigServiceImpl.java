package com.zjude.settings.service.impl;

import com.alibaba.fastjson2.JSON;
import com.zjude.common.enums.BannerLinkType;
import com.zjude.common.utils.StringUtils;
import com.zjude.settings.domain.MiniBanner;
import com.zjude.settings.domain.MiniBootstrapConfig;
import com.zjude.settings.domain.MiniPaymentMethod;
import com.zjude.settings.service.IMiniConfigService;
import com.zjude.settings.service.MiniPaymentPolicy;
import com.zjude.system.domain.ZjudgBanner;
import com.zjude.system.service.ISysConfigService;
import com.zjude.system.service.IZjudgBannerService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MiniConfigServiceImpl implements IMiniConfigService
{
    @Resource
    private ISysConfigService configService;

    @Resource
    private IZjudgBannerService bannerService;

    @Override
    public MiniBootstrapConfig bootstrap()
    {
        MiniBootstrapConfig result = new MiniBootstrapConfig();
        // Keep bootstrap limited to startup identity. Pages load their own
        // configuration through the dedicated endpoints below.
        result.setSite(site());
        result.setMini(mini());
        return result;
    }

    @Override
    public Map<String, Object> site()
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("shopName", value("website.shopName"));
        result.put("mobileLogo", value("website.mobileLogo"));
        result.put("memberDefaultAvatar", value("website.memberDefaultAvatar"));
        result.put("goodsDefaultImage", value("website.goodsDefaultImage"));
        return result;
    }

    private Map<String, Object> mini()
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", value("channel.mini.name"));
        result.put("qrcode", value("channel.mini.qrcode"));
        result.put("serviceStatus", value("channel.mini.serviceStatus"));
        return result;
    }

    @Override
    public Map<String, Object> navigation()
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", json("decorate.tabbar.items", List.of()));
        result.put("textColor", value("decorate.tabbar.textColor"));
        result.put("activeColor", value("decorate.tabbar.activeColor"));
        result.put("backgroundColor", value("decorate.tabbar.backgroundColor"));
        result.put("borderStyle", value("decorate.tabbar.borderStyle"));
        return result;
    }

    @Override
    public Map<String, Object> home()
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("navItems", json("decorate.home.navItems", List.of()));
        result.put("banners", json("decorate.home.banners", List.of()));
        result.put("hotWords", json("mall.hotSearch", List.of()));
        result.put("searchPlaceholder", value("decorate.home.searchPlaceholder"));
        result.put("seasonTitle", value("decorate.home.seasonTitle"));
        result.put("seasonSubtitle", value("decorate.home.seasonSubtitle"));
        result.put("rankingTitle", value("decorate.home.rankingTitle"));
        result.put("rankingSubtitle", value("decorate.home.rankingSubtitle"));
        result.put("moreText", value("decorate.home.moreText"));
        result.put("buyText", value("decorate.home.buyText"));
        result.put("cartSuccessText", value("decorate.home.cartSuccessText"));
        result.put("unavailableText", value("decorate.home.unavailableText"));
        result.put("mallLogo", value("decorate.home.mallLogo"));
        result.put("hotRanking", value("decorate.home.hotRanking"));
        result.put("newRecommend", value("decorate.home.newRecommend"));
        result.put("topBackground", value("decorate.home.topBackground"));
        return result;
    }

    @Override
    public Map<String, Object> category()
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("layout", value("decorate.category.layout"));
        result.put("bannerTitle", value("decorate.category.bannerTitle"));
        result.put("bannerSubtitle", value("decorate.category.bannerSubtitle"));
        result.put("bannerBackground", value("decorate.category.bannerBackground"));
        return result;
    }

    @Override
    public Map<String, Object> profile()
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("navItems", json("decorate.profile.navItems", List.of()));
        result.put("orderItems", json("decorate.profile.orderItems", List.of()));
        result.put("pageTitle", value("decorate.profile.pageTitle"));
        result.put("welcomeText", value("decorate.profile.welcomeText"));
        result.put("orderTitle", value("decorate.profile.orderTitle"));
        result.put("allOrdersText", value("decorate.profile.allOrdersText"));
        result.put("serviceTitle", value("decorate.profile.serviceTitle"));
        result.put("unavailableText", value("decorate.profile.unavailableText"));
        result.put("topBackground", value("decorate.profile.topBackground"));
        return result;
    }

    @Override
    public List<MiniPaymentMethod> paymentMethods()
    {
        return paymentMethods("WECHAT");
    }

    @Override
    public List<MiniPaymentMethod> paymentMethods(String platform)
    {
        List<MiniPaymentMethod> result = new ArrayList<>();
        String normalized = MiniPaymentPolicy.platform(platform);
        if (MiniPaymentPolicy.allows(normalized, "WECHAT"))
        {
            result.add(payment("WECHAT", "payment.wechat.name", "微信支付"));
        }
        if (MiniPaymentPolicy.allows(normalized, "ALIPAY"))
        {
            result.add(payment("ALIPAY", "payment.alipay.name", "支付宝"));
        }
        result.add(payment("BALANCE", "payment.balance.name", "余额支付"));
        return result;
    }

    @Override
    public Map<String, Object> uploadPolicy()
    {
        String provider = value("upload.storage.provider");
        if (StringUtils.isEmpty(provider))
        {
            provider = "LOCAL";
        }
        String domainKey = switch (provider.toUpperCase())
        {
            case "QINIU" -> "upload.qiniu.domain";
            case "ALIYUN" -> "upload.aliyun.domain";
            case "TENCENT" -> "upload.tencent.domain";
            default -> "upload.local.domain";
        };
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("provider", provider);
        result.put("domain", value(domainKey));
        return result;
    }

    @Override
    public List<MiniBanner> banners(String position)    {
        // 轮播为展示类内容，与城市维度无关；只按位置 + 启用状态 + 有效期过滤
        List<ZjudgBanner> rows = bannerService.selectPublishedBanners(position);
        List<MiniBanner> result = new ArrayList<>();
        for (ZjudgBanner row : rows)
        {
            MiniBanner item = new MiniBanner();
            item.setId(row.getBannerId());
            item.setTitle(row.getBannerTitle());
            item.setSubtitle(row.getRemark());
            item.setImage(row.getImageUrl());
            item.setLinkType(row.getLinkType());
            // 无跳转时不下发地址，避免前端把空串当成可跳转链接
            item.setLink(BannerLinkType.NONE.getCode().equals(row.getLinkType()) ? "" : row.getLinkUrl());
            result.add(item);
        }
        return result;
    }

    private MiniPaymentMethod payment(String type, String nameKey, String defaultName)
    {
        String status = value("payment." + type.toLowerCase() + ".status");
        return new MiniPaymentMethod(type, nonEmpty(value(nameKey), defaultName), "1".equals(status) || "true".equalsIgnoreCase(status));
    }

    private String value(String key)
    {
        String value = configService.selectConfigByKey(key);
        return value == null ? "" : value;
    }

    private Object json(String key, Object fallback)
    {
        String raw = value(key);
        if (StringUtils.isEmpty(raw))
        {
            return fallback;
        }
        try
        {
            return JSON.parse(raw);
        }
        catch (RuntimeException ignored)
        {
            return raw;
        }
    }

    private String nonEmpty(String value, String fallback)
    {
        return StringUtils.isEmpty(value) ? fallback : value;
    }
}
