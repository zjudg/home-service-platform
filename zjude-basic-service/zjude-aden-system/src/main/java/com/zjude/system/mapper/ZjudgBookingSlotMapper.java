package com.zjude.system.mapper;
import java.util.List;
import com.zjude.system.domain.ZjudgBookingSlot;
public interface ZjudgBookingSlotMapper {
    List<ZjudgBookingSlot> selectList(ZjudgBookingSlot query);
    ZjudgBookingSlot selectById(String id);
    int insert(ZjudgBookingSlot item);
    int update(ZjudgBookingSlot item);
    int deleteByIds(String[] ids);
}
