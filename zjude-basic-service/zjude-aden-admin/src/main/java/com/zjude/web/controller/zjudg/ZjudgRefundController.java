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
import com.zjude.system.domain.ZjudgRefund;
import com.zjude.system.service.IZjudgRefundService;

/**
 * 退款管理后台接口。
 */
@RestController
@RequestMapping("/zjudg/refund")
public class ZjudgRefundController extends BaseController
{
    @Resource
    private IZjudgRefundService refundService;

    /** 退款列表 */
    @PreAuthorize("@ss.hasPerm('zjudg:refund:list')")
    @GetMapping("/list")
    public TableDataInfo list(ZjudgRefund query)
    {
        startPage();
        List<ZjudgRefund> rows = refundService.selectList(query);
        return getDataTable(rows);
    }

    /** 同意退款并提交微信 */
    @PreAuthorize("@ss.hasPerm('zjudg:refund:edit')")
    @Log(title = "退款处理", businessType = BusinessType.UPDATE)
    @PutMapping("/{refundId}/approve")
    public AjaxResult approve(@PathVariable String refundId)
    {
        return success(refundService.approve(refundId, getUsername()));
    }

    /** 拒绝退款 */
    @PreAuthorize("@ss.hasPerm('zjudg:refund:edit')")
    @Log(title = "退款拒绝", businessType = BusinessType.UPDATE)
    @PutMapping("/{refundId}/reject")
    public AjaxResult reject(@PathVariable String refundId, @RequestBody RejectBody body)
    {
        return toAjax(refundService.reject(refundId, body.reason(), getUsername()));
    }

    /** 主动同步退款结果 */
    @PreAuthorize("@ss.hasPerm('zjudg:refund:edit')")
    @GetMapping("/{refundId}/sync")
    public AjaxResult sync(@PathVariable String refundId)
    {
        return success(refundService.syncStatus(refundId));
    }

    /** 拒绝原因请求体 */
    public record RejectBody(String reason)
    {
    }
}
