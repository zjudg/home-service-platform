package com.zjude.system.service;

import java.util.List;
import com.zjude.system.domain.ZjudgCertificateProduct;

public interface IZjudgCertificateProductService
{
    List<ZjudgCertificateProduct> selectList(ZjudgCertificateProduct query);
    List<ZjudgCertificateProduct> selectPublishedList();
    ZjudgCertificateProduct selectById(String id);
    int insert(ZjudgCertificateProduct item);
    int update(ZjudgCertificateProduct item);
    int deleteByIds(String[] ids);
}
