package com.zjude.web.controller.zjudg;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.annotation.Log;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.page.TableDataInfo;
import com.zjude.common.enums.BusinessType;
import com.zjude.system.domain.ZjudgMasterWithdraw;
import com.zjude.system.service.IZjudgMasterWithdrawService;

/**
 * 师傅提现管理后台接口。
 */
@RestController
@RequestMapping("/zjudg/withdraw")
public class ZjudgWithdrawController extends BaseController
{
    @Resource
    private IZjudgMasterWithdrawService withdrawService;

    /** 提现列表 */
    @PreAuthorize("@ss.hasPerm('zjudg:withdraw:list')")
    @GetMapping("/list")
    public TableDataInfo list(ZjudgMasterWithdraw query)
    {
        startPage();
        List<ZjudgMasterWithdraw> rows = withdrawService.selectList(query);
        return getDataTable(rows);
    }

    /** 审核通过 */
    @PreAuthorize("@ss.hasPerm('zjudg:withdraw:edit')")
    @Log(title = "提现审核", businessType = BusinessType.UPDATE)
    @PutMapping("/{withdrawId}/approve")
    public AjaxResult approve(@PathVariable String withdrawId, @RequestBody(required = false) RemarkBody body)
    {
        String remark = body == null ? null : body.remark();
        return toAjax(withdrawService.approve(withdrawId, getUsername(), remark));
    }

    /** 驳回 */
    @PreAuthorize("@ss.hasPerm('zjudg:withdraw:edit')")
    @Log(title = "提现驳回", businessType = BusinessType.UPDATE)
    @PutMapping("/{withdrawId}/reject")
    public AjaxResult reject(@PathVariable String withdrawId, @RequestBody RemarkBody body)
    {
        return toAjax(withdrawService.reject(withdrawId, getUsername(), body.remark()));
    }

    /** 标记已打款 */
    @PreAuthorize("@ss.hasPerm('zjudg:withdraw:edit')")
    @Log(title = "提现打款", businessType = BusinessType.UPDATE)
    @PutMapping("/{withdrawId}/paid")
    public AjaxResult paid(@PathVariable String withdrawId, @RequestBody(required = false) TransferBody body)
    {
        String transferNo = body == null ? null : body.transferNo();
        return toAjax(withdrawService.markPaid(withdrawId, transferNo, getUsername()));
    }

    /** 标记打款失败 */
    @PreAuthorize("@ss.hasPerm('zjudg:withdraw:edit')")
    @Log(title = "提现打款失败", businessType = BusinessType.UPDATE)
    @PutMapping("/{withdrawId}/failed")
    public AjaxResult failed(@PathVariable String withdrawId, @RequestBody RemarkBody body)
    {
        return toAjax(withdrawService.markFailed(withdrawId, body.remark(), getUsername()));
    }

    /** 备注请求体 */
    public record RemarkBody(String remark)
    {
    }

    /** 打款请求体 */
    public record TransferBody(String transferNo)
    {
    }
}
