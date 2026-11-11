package com.zjude.web.controller.zjudg;

import java.util.List;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.zjude.common.annotation.Log;
import com.zjude.common.annotation.RepeatSubmit;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.page.TableDataInfo;
import com.zjude.common.enums.BusinessType;
import com.zjude.system.domain.ZjudgOrder;
import com.zjude.system.service.IZjudgOrderService;

/**
 * 管理端订单管理。
 */
@RestController
@RequestMapping("/zjudg/order")
public class ZjudgOrderController extends BaseController
{
    @Resource
    private IZjudgOrderService service;

    @PreAuthorize("@ss.hasPerm('zjudg:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(ZjudgOrder query)
    {
        startPage();
        List<ZjudgOrder> rows = service.selectAdminList(query);
        return getDataTable(rows);
    }

    @PreAuthorize("@ss.hasPerm('zjudg:order:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable String id)
    {
        return success(service.selectAdminById(id));
    }

    /** 管理端将线下已收款需求发布为师傅可抢的订单，不创建微信支付单。 */
    @PreAuthorize("@ss.hasPerm('zjudg:order:add')")
    @Log(title = "线下订单录入", businessType = BusinessType.INSERT)
    @RepeatSubmit(interval = 3000, message = "订单正在录入，请勿重复提交")
    @PostMapping("/offline")
    public AjaxResult createOfflinePaidOrder(@Validated @RequestBody OfflineOrderBody body)
    {
        ZjudgOrder order = service.createOfflinePaidOrder(body.serviceId(), body.slotId(), body.appointmentDate(),
                body.quantity(), body.remark(), body.address(), body.phone(), body.customerName(), body.cityId(),
                getUsername());
        return AjaxResult.success("线下已收款需求已发布到师傅接单大厅", order);
    }

    /**
     * 管理端流转订单状态。
     *
     * @param id 订单主键
     * @param orderStatus 目标状态编码
     * @return 操作结果
     */
    @PreAuthorize("@ss.hasPerm('zjudg:order:edit')")
    @Log(title = "家政订单", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/status")
    public AjaxResult updateStatus(@PathVariable String id, @Validated @RequestBody StatusBody orderStatus)
    {
        return toAjax(service.updateStatus(id, orderStatus.orderStatus(), getUsername()));
    }

    @PreAuthorize("@ss.hasPerm('zjudg:order:remove')")
    @Log(title = "家政订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable String id)
    {
        ZjudgOrder query = new ZjudgOrder();
        query.setOrderId(id);
        return toAjax(service.deleteAdminOrder(query));
    }

    /** 状态流转请求体。 */
    public record StatusBody(@jakarta.validation.constraints.NotBlank(message = "目标状态不能为空") String orderStatus)
    {
    }

    /** 线下已收款订单信息；订单金额始终由服务端目录价格计算。 */
    public record OfflineOrderBody(
            @NotBlank(message = "请选择服务项目") @Size(max = 64) String serviceId,
            @NotBlank(message = "请选择预约时段") @Size(max = 64) String slotId,
            @NotBlank(message = "请选择预约日期") @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "预约日期格式应为 yyyy-MM-dd") String appointmentDate,
            @NotBlank(message = "请选择服务城市") @Size(max = 16) String cityId,
            @NotBlank(message = "请填写客户姓名") @Size(max = 128) String customerName,
            @NotBlank(message = "请填写联系电话") @Pattern(regexp = "^1\\d{10}$", message = "联系电话格式不正确") String phone,
            @NotBlank(message = "请填写服务地址") @Size(max = 255) String address,
            @DecimalMin(value = "0.01", message = "服务数量必须大于0") java.math.BigDecimal quantity,
            @Size(max = 500, message = "备注不能超过500个字符") String remark)
    {
    }
}
