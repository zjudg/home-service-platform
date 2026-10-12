package com.zjude.system.service;
import java.util.List;
import com.zjude.system.domain.ZjudgBookingSlot;
public interface IZjudgBookingService { List<ZjudgBookingSlot> selectList(ZjudgBookingSlot q); ZjudgBookingSlot selectById(String id); int insert(ZjudgBookingSlot item); int update(ZjudgBookingSlot item); int deleteByIds(String[] ids); }
