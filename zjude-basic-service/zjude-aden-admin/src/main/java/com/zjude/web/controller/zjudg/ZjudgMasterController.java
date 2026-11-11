package com.zjude.web.controller.zjudg;

import java.util.List;
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
import com.zjude.system.domain.ZjudgMaster;
import com.zjude.system.service.IZjudgMasterService;

/**
 * 家政师傅管理后台接口。
 *
 * <p>传输层仅做参数整形与结果包装，审核流转等业务约束由
 * {@link IZjudgMasterService} 负责。</p>
 */
@RestController
@RequestMapping("/zjudg/master")
public class ZjudgMasterController extends BaseController
{
    @Resource
    private IZjudgMasterService service;

    /** 师傅列表 */
    @PreAuthorize("@ss.hasPerm('zjudg:master:list')")
    @GetMapping("/list")
    public TableDataInfo list(ZjudgMaster query)
    {
        startPage();
        List<ZjudgMaster> rows = service.selectList(query);
        return getDataTable(rows);
    }

    /** 师傅详情 */
    @PreAuthorize("@ss.hasPerm('zjudg:master:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable String id)
    {
        return success(service.selectById(id));
    }

    /** 新增师傅 */
    @PreAuthorize("@ss.hasPerm('zjudg:master:add')")
    @Log(title = "家政师傅", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ZjudgMaster item)
    {
        item.setCreateBy(getUsername());
        return toAjax(service.insert(item));
    }

    /** 编辑师傅 */
    @PreAuthorize("@ss.hasPerm('zjudg:master:edit')")
    @Log(title = "家政师傅", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ZjudgMaster item)
    {
        item.setUpdateBy(getUsername());
        return toAjax(service.update(item));
    }

    /** 审核师傅入驻申请 */
    @PreAuthorize("@ss.hasPerm('zjudg:master:audit')")
    @Log(title = "家政师傅审核", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public AjaxResult audit(@Validated @RequestBody AuditBody body)
    {
        return toAjax(service.audit(body.masterId(), body.auditStatus(), body.auditRemark(), getUsername()));
    }

    /** 批量删除师傅 */
    @PreAuthorize("@ss.hasPerm('zjudg:master:remove')")
    @Log(title = "家政师傅", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(service.deleteByIds(ids));
    }

    /** 审核请求体 */
    public record AuditBody(String masterId, String auditStatus, String auditRemark)
    {
    }
}
