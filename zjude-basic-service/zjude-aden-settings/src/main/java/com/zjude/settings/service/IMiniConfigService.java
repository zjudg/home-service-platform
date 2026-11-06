package com.zjude.settings.service;

import com.zjude.settings.domain.MiniBanner;
import com.zjude.settings.domain.MiniBootstrapConfig;
import com.zjude.settings.domain.MiniPaymentMethod;
import com.zjude.settings.domain.MiniLocationInfo;
import java.math.BigDecimal;
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

    /**
     * 小程序订阅消息模板 ID（公开模板标识，不包含 AppSecret 等凭据），供客户端发起授权。
     *
     * @return 已启用的订阅消息配置
     */
    Map<String, Object> subscriptionTemplates();

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

    /**
     * 根据坐标查询天地图行政区划信息。
     *
     * @param longitude 经度
     * @param latitude 纬度
     * @return 省、市、区及格式化地址
     */
    MiniLocationInfo reverseGeocode(BigDecimal longitude, BigDecimal latitude);
}
