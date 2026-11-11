package com.zjude.system.service;

import java.util.List;
import java.util.Map;
import com.zjude.system.domain.ZjudgCertificateApplication;
import com.zjude.system.domain.ZjudgCertificateProduct;

public interface IZjudgBusinessPaymentService
{
    List<ZjudgCertificateProduct> listProducts();
    List<ZjudgCertificateProduct> listAllProducts(ZjudgCertificateProduct query);
    ZjudgCertificateApplication createCertificateApplication(String productId, String userId, String phone, String photoUrl, String address);
    List<ZjudgCertificateApplication> listApplications();
    ZjudgCertificateApplication getApplicationForUser(String id, String userId);
    int updateApplication(String id, String status, String trackingNo);
    Map<String, Object> createPayment(String bizType, String bizId, String userId);
    boolean syncPayment(String bizType, String bizId, String userId);
    boolean confirmByWechat(String outTradeNo);
    boolean handleNotify(String body, String serial, String nonce, String signature, String timestamp);
}
