package com.zjude.settings.domain;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(name = "MiniBootstrapConfig", description = "小程序启动配置")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MiniBootstrapConfig
{
    @Schema(description = "站点公开配置") private Map<String, Object> site;
    @Schema(description = "小程序渠道公开配置") private Map<String, Object> mini;
    @Schema(description = "底部导航配置") private Map<String, Object> navigation;
    @Schema(description = "首页装修配置") private Map<String, Object> home;
    @Schema(description = "分类页配置") private Map<String, Object> category;
    @Schema(description = "个人中心配置") private Map<String, Object> profile;
    @Schema(description = "可用支付方式") private List<MiniPaymentMethod> paymentMethods;
    @Schema(description = "上传配置") private Map<String, Object> uploadPolicy;

    public Map<String, Object> getSite() { return site; }
    public void setSite(Map<String, Object> value) { site = value; }
    public Map<String, Object> getMini() { return mini; }
    public void setMini(Map<String, Object> value) { mini = value; }
    public Map<String, Object> getNavigation() { return navigation; }
    public void setNavigation(Map<String, Object> value) { navigation = value; }
    public Map<String, Object> getHome() { return home; }
    public void setHome(Map<String, Object> value) { home = value; }
    public Map<String, Object> getCategory() { return category; }
    public void setCategory(Map<String, Object> value) { category = value; }
    public Map<String, Object> getProfile() { return profile; }
    public void setProfile(Map<String, Object> value) { profile = value; }
    public List<MiniPaymentMethod> getPaymentMethods() { return paymentMethods; }
    public void setPaymentMethods(List<MiniPaymentMethod> value) { paymentMethods = value; }
    public Map<String, Object> getUploadPolicy() { return uploadPolicy; }
    public void setUploadPolicy(Map<String, Object> value) { uploadPolicy = value; }
}
