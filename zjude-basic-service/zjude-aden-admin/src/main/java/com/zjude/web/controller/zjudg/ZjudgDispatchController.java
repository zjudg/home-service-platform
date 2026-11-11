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
import com.zjude.system.domain.ZjudgDispatchRecord;
import com.zjude.system.service.IZjudgDispatchService;

/**
 * 派单管理后台接口。
 */
@RestController
@RequestMapping("/zjudg/dispatch")
public class ZjudgDispatchController extends BaseController
{
    @Resource
    private IZjudgDispatchService dispatchService;

    /** 派单记录列表 */
    @PreAuthorize("@ss.hasPerm('zjudg:dispatch:list')")
    @GetMapping("/list")
    public TableDataInfo list(ZjudgDispatchRecord query)
    {
        startPage();
        List<ZjudgDispatchRecord> rows = dispatchService.selectRecords(query);
        return getDataTable(rows);
    }

    /** 评估订单候选师傅（不产生派单动作） */
    @PreAuthorize("@ss.hasPerm('zjudg:dispatch:list')")
    @GetMapping("/candidates/{orderId}")
    public AjaxResult candidates(@PathVariable String orderId)
    {
        return success(dispatchService.evaluateCandidates(orderId));
    }

    /** 触发订单自动派单 */
    @PreAuthorize("@ss.hasPerm('zjudg:dispatch:edit')")
    @Log(title = "自动派单", businessType = BusinessType.UPDATE)
    @PutMapping("/auto/{orderId}")
    public AjaxResult autoDispatch(@PathVariable String orderId)
    {
        String masterId = dispatchService.autoDispatch(orderId);
        if (masterId == null)
        {
            return AjaxResult.error("未找到可用师傅，请手动指派");
        }
        return AjaxResult.success("派单成功", masterId);
    }

    /** 手动指派 */
    @PreAuthorize("@ss.hasPerm('zjudg:dispatch:edit')")
    @Log(title = "手动派单", businessType = BusinessType.UPDATE)
    @PutMapping("/manual")
    public AjaxResult manualDispatch(@RequestBody ManualBody body)
    {
        dispatchService.manualDispatch(body.orderId(), body.masterId(), getUsername());
        return AjaxResult.success("指派成功");
    }

    /** 手动派单请求体 */
    public record ManualBody(String orderId, String masterId)
    {
    }
}
