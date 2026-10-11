package com.zjude.web.controller.mall;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.annotation.Log;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.page.TableDataInfo;
import com.zjude.common.enums.BusinessType;
import com.zjude.common.exception.ServiceException;
import com.zjude.system.domain.MallContentItem;
import com.zjude.system.service.IMallContentItemService;

/** 公共内容资源管理接口。 */
@RestController
@RequestMapping("/mall/content")
public class MallContentController extends BaseController
{
    private static final Map<String, String> TYPES = Map.of(
            "AD", "AD", "SLOT", "SLOT", "HELP", "HELP",
            "HELP_CAT", "HELP_CAT", "ARTICLE", "ARTICLE", "ARTICLE_CAT", "ARTICLE_CAT");

    @Resource
    private IMallContentItemService contentService;
    @PreAuthorize("@ss.hasPerm('settings:content:list')")
    @GetMapping("/{type}/list")
    public TableDataInfo list(@PathVariable String type, MallContentItem item)
    {
        item.setResourceType(normalizeType(type));
        startPage();
        List<MallContentItem> list = contentService.selectContentList(item);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPerm('settings:content:list')")
    @GetMapping("/{type}/{contentId}")
    public AjaxResult getInfo(@PathVariable String type, @PathVariable String contentId)
    {
        MallContentItem item = contentService.selectContentById(contentId);
        if (item == null || !normalizeType(type).equals(item.getResourceType())) return error("内容不存在");
        return success(item);
    }

    @PreAuthorize("@ss.hasPerm('settings:content:add')")
    @Log(title = "内容管理", businessType = BusinessType.INSERT)
    @PostMapping("/{type}")
    public AjaxResult add(@PathVariable String type, @RequestBody MallContentItem item)
    {
        item.setResourceType(normalizeType(type));
        item.setCreateBy(getUsername());
        return toAjax(contentService.insertContent(item));
    }

    @PreAuthorize("@ss.hasPerm('settings:content:edit')")
    @Log(title = "内容管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{type}")
    public AjaxResult edit(@PathVariable String type, @RequestBody MallContentItem item)
    {
        String resourceType = normalizeType(type);
        MallContentItem existing = contentService.selectContentById(item.getContentId());
        if (existing == null || !resourceType.equals(existing.getResourceType())) return error("内容不存在");
        item.setResourceType(resourceType);
        item.setUpdateBy(getUsername());
        return toAjax(contentService.updateContent(item));
    }

    @PreAuthorize("@ss.hasPerm('settings:content:delete')")
    @Log(title = "内容管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{type}/{contentId}")
    public AjaxResult remove(@PathVariable String type, @PathVariable String contentId)
    {
        String resourceType = normalizeType(type);
        MallContentItem existing = contentService.selectContentById(contentId);
        if (existing == null || !resourceType.equals(existing.getResourceType())) return error("内容不存在");
        return toAjax(contentService.deleteContentById(contentId));
    }

    private String normalizeType(String type)
    {
        String normalized = type == null ? "" : type.toUpperCase(Locale.ROOT);
        if (!TYPES.containsKey(normalized)) throw new ServiceException("不支持的内容类型：" + type);
        return TYPES.get(normalized);
    }
}
