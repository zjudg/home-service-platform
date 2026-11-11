package com.zjude.system.service;

import java.util.List;
import com.zjude.system.domain.ZjudgContentItem;

public interface IZjudgContentItemService
{
    List<ZjudgContentItem> selectContentList(ZjudgContentItem item);
    ZjudgContentItem selectContentById(String contentId);
    int insertContent(ZjudgContentItem item);
    int updateContent(ZjudgContentItem item);
    int deleteContentById(String contentId);
}
