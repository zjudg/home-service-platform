package com.zjude.settings.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MiniPaymentMethod", description = "小程序可用支付方式")
public class MiniPaymentMethod
{
    @Schema(description = "支付方式编码", example = "WECHAT") private String type;
    @Schema(description = "支付方式名称", example = "微信支付") private String name;
    @Schema(description = "是否启用") private boolean enabled;

    public MiniPaymentMethod() { }
    public MiniPaymentMethod(String type, String name, boolean enabled) { this.type = type; this.name = name; this.enabled = enabled; }
    public String getType() { return type; }
    public String getName() { return name; }
    public boolean isEnabled() { return enabled; }
}
