package com.zjude.system.service.impl;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.ZjudgBookingSlot;
import com.zjude.system.mapper.ZjudgBookingSlotMapper;
import com.zjude.system.service.IZjudgBookingService;

@Service("zjudgBookingService")
public class ZjudgBookingServiceImpl implements IZjudgBookingService {
    @Resource private ZjudgBookingSlotMapper mapper;
    @Resource private BaseKeyGenerate keyGenerate;
    public List<ZjudgBookingSlot> selectList(ZjudgBookingSlot q) { return mapper.selectList(q); }
    public ZjudgBookingSlot selectById(String id) { return mapper.selectById(id); }
    public int insert(ZjudgBookingSlot item) { check(item); item.setSlotId(keyGenerate.snowflakeStringKey("ZB_")); return mapper.insert(item); }
    public int update(ZjudgBookingSlot item) { check(item); return mapper.update(item); }
    public int deleteByIds(String[] ids) { return mapper.deleteByIds(ids); }
    private void check(ZjudgBookingSlot item) { if (item.getStartTime() != null && item.getEndTime() != null && !item.getStartTime().isBefore(item.getEndTime())) throw new ServiceException("开始时间必须早于结束时间"); }
}
