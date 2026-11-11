package com.zjude.system.service;

import com.zjude.system.domain.ZjudgCertificateApplication;

public interface IZjudgCertificateApplicationService
{
    ZjudgCertificateApplication create(String productId, String userId, String phone, String photoUrl, String address);
    ZjudgCertificateApplication getForUser(String applicationId, String userId);
    ZjudgCertificateApplication get(String applicationId);
    boolean markPaid(String applicationId);
}
