package com.zjude.settings.service;

import com.zjude.settings.domain.MiniBanner;
import com.zjude.settings.domain.MiniBootstrapConfig;
import com.zjude.settings.domain.MiniPaymentMethod;
import java.util.List;
import java.util.Map;

public interface IMiniConfigService
{
    MiniBootstrapConfig bootstrap();

    Map<String, Object> site();

    Map<String, Object> navigation();

    Map<String, Object> home();

    Map<String, Object> category();
    Map<String, Object> profile();

    List<MiniPaymentMethod> paymentMethods();

    List<MiniPaymentMethod> paymentMethods(String platform);

    Map<String, Object> uploadPolicy();

    /**
     * 查询指定广告位置下当前生效的轮播图。
     *
     * @param position 广告位置（index/category/activity），为空时按首页处理
     * @return 轮播列表，无数据时返回空列表
     */
    List<MiniBanner> banners(String position);
}
