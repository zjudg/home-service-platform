package com.zjude.settings.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MiniLocationInfo", description = "定位逆地理编码结果")
public class MiniLocationInfo
{
    @Schema(description = "已开通服务城市ID")
    private String cityId;

    @Schema(description = "省级行政区")
    private String provinceName;

    @Schema(description = "服务城市名称")
    private String cityName;

    @Schema(description = "区县名称")
    private String districtName;

    @Schema(description = "格式化地址")
    private String formattedAddress;

    public String getCityId() { return cityId; }
    public void setCityId(String value) { cityId = value; }
    public String getProvinceName() { return provinceName; }
    public void setProvinceName(String value) { provinceName = value; }
    public String getCityName() { return cityName; }
    public void setCityName(String value) { cityName = value; }
    public String getDistrictName() { return districtName; }
    public void setDistrictName(String value) { districtName = value; }
    public String getFormattedAddress() { return formattedAddress; }
    public void setFormattedAddress(String value) { formattedAddress = value; }
}
