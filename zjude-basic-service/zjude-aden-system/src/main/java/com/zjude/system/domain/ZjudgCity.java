package com.zjude.system.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 家政服务开通城市。
 *
 * <p>城市是服务发布、轮播投放与派单范围的公共维度，
 * 后台维护、线下导入与小程序下拉共用同一份数据。</p>
 */
public class ZjudgCity {
    private String cityId;

    @NotBlank(message = "城市名称不能为空")
    @Size(max = 64, message = "城市名称长度不能超过64个字符")
    private String cityName;

    @NotBlank(message = "所属省份不能为空")
    @Size(max = 64, message = "所属省份长度不能超过64个字符")
    private String provinceName;

    @NotBlank(message = "首字母不能为空")
    @Size(max = 1, message = "首字母只能是一个字符")
    private String initialLetter;

    @Pattern(regexp = "[YN]", message = "热门城市标记无效")
    private String isHot;

    private String status;

    public String getCityId() { return cityId; }
    public void setCityId(String value) { cityId = value; }
    public String getCityName() { return cityName; }
    public void setCityName(String value) { cityName = value; }
    public String getProvinceName() { return provinceName; }
    public void setProvinceName(String value) { provinceName = value; }
    public String getInitialLetter() { return initialLetter; }
    public void setInitialLetter(String value) { initialLetter = value; }
    public String getIsHot() { return isHot; }
    public void setIsHot(String value) { isHot = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
}
