package com.zjude.system.service.impl;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.ZjudgService;
import com.zjude.system.mapper.ZjudgServiceMapper;
import com.zjude.system.service.IZjudgServiceService;

@Service("zjudgServiceService")
public class ZjudgServiceServiceImpl implements IZjudgServiceService {
    @Resource private ZjudgServiceMapper mapper;
    @Resource private BaseKeyGenerate keyGenerate;
    public List<ZjudgService> selectList(ZjudgService q) { return mapper.selectList(q); }
    public ZjudgService selectById(String id) { return mapper.selectById(id); }
    public int insert(ZjudgService item) { check(item); item.setServiceId(keyGenerate.snowflakeStringKey("ZS_")); return mapper.insert(item); }
    public int update(ZjudgService item) { check(item); return mapper.update(item); }
    public int deleteByIds(String[] ids) { return mapper.deleteByIds(ids); }
    private void check(ZjudgService item) {
        if (item.getPrice() != null && item.getOriginalPrice() != null && item.getOriginalPrice().compareTo(item.getPrice()) < 0) throw new ServiceException("划线价不能低于服务价格");
        if (!"0".equals(item.getStatus()) && !"1".equals(item.getStatus())) throw new ServiceException("服务状态不合法");
    }
}
