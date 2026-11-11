package com.zjude.system.mapper;

import java.util.List;
import com.zjude.system.domain.ZjudgContentItem;

public interface ZjudgContentItemMapper
{
    List<ZjudgContentItem> selectContentList(ZjudgContentItem item);
    ZjudgContentItem selectContentById(String contentId);
    int insertContent(ZjudgContentItem item);
    int updateContent(ZjudgContentItem item);
    int deleteContentById(String contentId);
}
