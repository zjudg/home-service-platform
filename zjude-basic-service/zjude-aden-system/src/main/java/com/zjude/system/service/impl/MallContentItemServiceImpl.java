package com.zjude.system.service.impl;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.MallContentItem;
import com.zjude.system.mapper.MallContentItemMapper;
import com.zjude.system.service.IMallContentItemService;

@Service("mallContentItemService")
public class MallContentItemServiceImpl implements IMallContentItemService
{
    @Resource
    private MallContentItemMapper mapper;
    @Resource
    private BaseKeyGenerate keyGenerate;

    @Override public List<MallContentItem> selectContentList(MallContentItem item) { return mapper.selectContentList(item); }
    @Override public MallContentItem selectContentById(String id) { return mapper.selectContentById(id); }
    @Override public int insertContent(MallContentItem item)
    {
        item.setContentId(keyGenerate.snowflakeStringKey("C_"));
        return mapper.insertContent(item);
    }
    @Override public int updateContent(MallContentItem item) { return mapper.updateContent(item); }
    @Override public int deleteContentById(String id) { return mapper.deleteContentById(id); }
}
