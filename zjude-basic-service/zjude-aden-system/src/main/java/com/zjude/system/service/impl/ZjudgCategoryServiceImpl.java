package com.zjude.system.service.impl;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.ZjudgServiceCategory;
import com.zjude.system.mapper.ZjudgServiceCategoryMapper;
import com.zjude.system.service.IZjudgCategoryService;

@Service("zjudgCategoryService")
public class ZjudgCategoryServiceImpl implements IZjudgCategoryService {
    @Resource private ZjudgServiceCategoryMapper mapper;
    @Resource private BaseKeyGenerate keyGenerate;
    public List<ZjudgServiceCategory> selectList(ZjudgServiceCategory q) { return mapper.selectList(q); }
    public ZjudgServiceCategory selectById(String id) { return mapper.selectById(id); }
    public int insert(ZjudgServiceCategory item) { check(item); item.setCategoryId(keyGenerate.snowflakeStringKey("ZC_")); return mapper.insert(item); }
    public int update(ZjudgServiceCategory item) { check(item); return mapper.update(item); }
    public int deleteByIds(String[] ids) { return mapper.deleteByIds(ids); }
    private void check(ZjudgServiceCategory item) { if (!"0".equals(item.getStatus()) && !"1".equals(item.getStatus())) throw new ServiceException("分类状态不合法"); }
}
