package com.zjude.settings.service;

import java.util.Locale;
import java.util.Set;

/** Payment channels exposed by each client platform. */
public final class MiniPaymentPolicy
{
    private MiniPaymentPolicy() { }

    public static String platform(String value)
    {
        if (value == null || value.isBlank()) return "WECHAT";
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        if (normalized.contains("ALIPAY")) return "ALIPAY";
        if (normalized.contains("APP") || normalized.contains("PLUS")) return "APP";
        return "WECHAT";
    }

    public static boolean allows(String platform, String provider)
    {
        String normalizedProvider = provider == null ? "" : provider.trim().toUpperCase(Locale.ROOT);
        return switch (platform(platform))
        {
            case "ALIPAY" -> Set.of("ALIPAY", "BALANCE").contains(normalizedProvider);
            case "APP" -> Set.of("WECHAT", "ALIPAY", "BALANCE").contains(normalizedProvider);
            default -> Set.of("WECHAT", "BALANCE").contains(normalizedProvider);
        };
    }
}
