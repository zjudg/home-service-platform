package com.zjude.settings.service;

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
}
