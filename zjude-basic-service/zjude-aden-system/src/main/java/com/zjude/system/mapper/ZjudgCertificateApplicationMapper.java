package com.zjude.system.mapper;

import org.apache.ibatis.annotations.Param;
import com.zjude.system.domain.ZjudgCertificateApplication;

public interface ZjudgCertificateApplicationMapper
{
    java.util.List<ZjudgCertificateApplication> selectList(ZjudgCertificateApplication query);
    int insert(ZjudgCertificateApplication item);
    ZjudgCertificateApplication selectById(String id);
    ZjudgCertificateApplication selectByIdAndUser(@Param("id") String id, @Param("userId") String userId);
    int updateStatus(@Param("id") String id, @Param("fromStatus") String fromStatus, @Param("toStatus") String toStatus);
    int updateFulfillment(@Param("id") String id, @Param("fromStatus") String fromStatus,
            @Param("trackingNo") String trackingNo, @Param("status") String status);
}
