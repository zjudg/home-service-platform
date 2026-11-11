package com.zjude.settings.domain;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Schema(name = "MiniReverseGeocodeRequest", description = "小程序定位坐标")
public class MiniReverseGeocodeRequest
{
    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度超出有效范围")
    @DecimalMax(value = "180.0", message = "经度超出有效范围")
    @Schema(description = "经度（WGS84）", example = "116.397128")
    private BigDecimal longitude;

    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度超出有效范围")
    @DecimalMax(value = "90.0", message = "纬度超出有效范围")
    @Schema(description = "纬度（WGS84）", example = "39.916527")
    private BigDecimal latitude;

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal value) { longitude = value; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal value) { latitude = value; }
}
