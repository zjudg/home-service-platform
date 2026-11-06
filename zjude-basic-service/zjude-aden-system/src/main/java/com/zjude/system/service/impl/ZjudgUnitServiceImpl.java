package com.zjude.system.service.impl;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.enums.ZjudgUnitMeasureType;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.ZjudgServiceUnit;
import com.zjude.system.mapper.ZjudgServiceUnitMapper;
import com.zjude.system.service.IZjudgUnitService;

@Service("zjudgUnitService")
public class ZjudgUnitServiceImpl implements IZjudgUnitService {
    @Resource private ZjudgServiceUnitMapper mapper;
    @Resource private BaseKeyGenerate keyGenerate;
    public List<ZjudgServiceUnit> selectList(ZjudgServiceUnit q) { return mapper.selectList(q); }
    public ZjudgServiceUnit selectById(String id) { return mapper.selectById(id); }
    public int insert(ZjudgServiceUnit item) { check(item); item.setUnitId(keyGenerate.snowflakeStringKey("ZU_")); return mapper.insert(item); }
    public int update(ZjudgServiceUnit item) { check(item); return mapper.update(item); }
    public int deleteByIds(String[] ids) { return mapper.deleteByIds(ids); }
    private void check(ZjudgServiceUnit item) {
        if (!"0".equals(item.getStatus()) && !"1".equals(item.getStatus())) throw new ServiceException("单位状态不合法");
        if (!ZjudgUnitMeasureType.isValid(item.getMeasureType())) throw new ServiceException("单位计量方式不合法");
    }
}
