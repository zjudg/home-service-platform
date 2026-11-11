package com.zjude.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zjude.system.domain.ZjudgOrderExtraTime;

public interface ZjudgOrderExtraTimeMapper
{
    int insert(ZjudgOrderExtraTime item);
    ZjudgOrderExtraTime selectById(String id);
    ZjudgOrderExtraTime selectByIdAndUser(@Param("id") String id, @Param("userId") String userId);
    List<ZjudgOrderExtraTime> selectByOrderAndUser(@Param("orderId") String orderId, @Param("userId") String userId);
    int markPaid(@Param("id") String id);
}
