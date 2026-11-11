package com.zjude.web.controller.zjudg;

import java.util.List;
import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
import com.zjude.common.utils.SecurityUtils;
import com.zjude.system.domain.ZjudgMaterial;
import com.zjude.system.service.IZjudgMaterialService;

/**
 * 素材中心后台接口。
 */
@RestController
@RequestMapping("/zjudg/material")
public class ZjudgMaterialController extends BaseController
{
    @Resource
    private IZjudgMaterialService materialService;

    /** 素材列表 */
    @PreAuthorize("@ss.hasPerm('zjudg:material:list')")
    @GetMapping("/list")
    public TableDataInfo list(ZjudgMaterial query)
    {
        startPage();
        return getDataTable(materialService.list(query));
    }

    /** 素材详情 */
    @PreAuthorize("@ss.hasPerm('zjudg:material:query')")
    @GetMapping("/{materialId}")
    public AjaxResult getInfo(@PathVariable String materialId)
    {
        return success(materialService.detail(materialId));
    }

    /** 新增素材 */
    @PreAuthorize("@ss.hasPerm('zjudg:material:add')")
    @Log(title = "素材中心", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ZjudgMaterial material)
    {
        return success(materialService.add(material, SecurityUtils.getUsername()));
    }

    /** 修改素材 */
    @PreAuthorize("@ss.hasPerm('zjudg:material:edit')")
    @Log(title = "素材中心", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ZjudgMaterial material)
    {
        return toAjax(materialService.update(material, SecurityUtils.getUsername()));
    }

    /** 批量删除素材 */
    @PreAuthorize("@ss.hasPerm('zjudg:material:remove')")
    @Log(title = "素材中心", businessType = BusinessType.DELETE)
    @DeleteMapping("/{materialIds}")
    public AjaxResult remove(@PathVariable List<String> materialIds)
    {
        return toAjax(materialService.remove(materialIds));
    }

    /** 批量移动素材到指定分组 */
    @PreAuthorize("@ss.hasPerm('zjudg:material:edit')")
    @Log(title = "素材中心", businessType = BusinessType.UPDATE)
    @PutMapping("/move")
    public AjaxResult move(@RequestBody Map<String, Object> payload)
    {
        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) payload.get("materialIds");
        return toAjax(materialService.move(ids, String.valueOf(payload.getOrDefault("groupId", ""))));
    }

    /** 分组列表，含每个分组的素材数量 */
    @PreAuthorize("@ss.hasPerm('zjudg:material:list')")
    @GetMapping("/groups")
    public AjaxResult groups()
    {
        return success(materialService.groups());
    }

    /** 新增分组 */
    @PreAuthorize("@ss.hasPerm('zjudg:material:add')")
    @Log(title = "素材中心", businessType = BusinessType.INSERT)
    @PostMapping("/groups")
    public AjaxResult addGroup(@RequestBody Map<String, String> payload)
    {
        return success(materialService.addGroup(payload.get("groupName")));
    }

    /** 重命名分组 */
    @PreAuthorize("@ss.hasPerm('zjudg:material:edit')")
    @Log(title = "素材中心", businessType = BusinessType.UPDATE)
    @PutMapping("/groups/{groupId}")
    public AjaxResult renameGroup(@PathVariable String groupId, @RequestBody Map<String, String> payload)
    {
        return toAjax(materialService.renameGroup(groupId, payload.get("groupName")));
    }

    /** 删除分组，组内素材迁移到「未分组」 */
    @PreAuthorize("@ss.hasPerm('zjudg:material:remove')")
    @Log(title = "素材中心", businessType = BusinessType.DELETE)
    @DeleteMapping("/groups/{groupId}")
    public AjaxResult removeGroup(@PathVariable String groupId)
    {
        return success(materialService.removeGroup(groupId));
    }
}
