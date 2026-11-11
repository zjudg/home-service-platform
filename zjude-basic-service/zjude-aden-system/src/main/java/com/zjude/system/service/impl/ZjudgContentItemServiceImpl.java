package com.zjude.system.service.impl;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.ZjudgContentItem;
import com.zjude.system.mapper.ZjudgContentItemMapper;
import com.zjude.system.service.IZjudgContentItemService;

@Service("ZjudgContentItemService")
public class ZjudgContentItemServiceImpl implements IZjudgContentItemService
{
    @Resource
    private ZjudgContentItemMapper mapper;
    @Resource
    private BaseKeyGenerate keyGenerate;

    @Override public List<ZjudgContentItem> selectContentList(ZjudgContentItem item) { return mapper.selectContentList(item); }
    @Override public ZjudgContentItem selectContentById(String id) { return mapper.selectContentById(id); }
    @Override public int insertContent(ZjudgContentItem item)
    {
        item.setContentId(keyGenerate.snowflakeStringKey("C_"));
        return mapper.insertContent(item);
    }
    @Override public int updateContent(ZjudgContentItem item) { return mapper.updateContent(item); }
    @Override public int deleteContentById(String id) { return mapper.deleteContentById(id); }
}
