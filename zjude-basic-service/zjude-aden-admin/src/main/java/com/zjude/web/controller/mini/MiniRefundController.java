package com.zjude.web.controller.mini;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.utils.SecurityUtils;
import com.zjude.system.domain.ZjudgRefund;
import com.zjude.system.service.IZjudgRefundService;

/**
 * 小程序退款接口。
 *
 * <p>用户只能查看与申请自己订单的退款，订单归属校验在服务层完成。</p>
 */
@RestController
@RequestMapping("/api/mini/refund")
public class MiniRefundController extends BaseController
{
    @Resource
    private IZjudgRefundService refundService;

    /**
     * 申请退款。
     *
     * @param orderId 订单主键
     * @param body 申请原因
     * @return 退款单
     */
    @PostMapping("/orders/{orderId}")
    public AjaxResult apply(@PathVariable String orderId, @Valid @RequestBody(required = false) ApplyBody body)
    {
        String reason = body == null ? null : body.reason();
        ZjudgRefund refund = refundService.applyRefund(orderId, SecurityUtils.getUserId(), reason);
        return AjaxResult.success("退款申请已提交，请等待平台处理", refund);
    }

    /**
     * 查询订单退款进度。
     *
     * @param orderId 订单主键
     * @return 退款单，未申请过时 data 为 null
     */
    @GetMapping("/orders/{orderId}")
    public AjaxResult detail(@PathVariable String orderId)
    {
        return AjaxResult.success(refundService.getByOrder(orderId, SecurityUtils.getUserId()));
    }

    /** 退款申请请求体 */
    public record ApplyBody(
            @Size(max = 500, message = "退款原因不能超过500个字符") String reason)
    {
    }
}
