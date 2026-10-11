package com.zjude.integrated.app;

public record AppRuntimeConfig(
        String iosDownloadUrl,
        String androidDownloadUrl,
        String downloadText,
        boolean privacyAgreementRequired,
        boolean wechatLoginEnabled)
{
}
