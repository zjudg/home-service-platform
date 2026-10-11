package com.zjude.system.mapper;

import java.util.List;
import com.zjude.system.domain.MallContentItem;

public interface MallContentItemMapper
{
    List<MallContentItem> selectContentList(MallContentItem item);
    MallContentItem selectContentById(String contentId);
    int insertContent(MallContentItem item);
    int updateContent(MallContentItem item);
    int deleteContentById(String contentId);
}
