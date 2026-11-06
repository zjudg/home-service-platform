package com.zjude.web.controller.mini;

import java.math.BigDecimal;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
import com.zjude.system.domain.ZjudgCertificateProduct;
import com.zjude.system.service.IZjudgBusinessPaymentService;
import com.zjude.system.mapper.SysUserMapper;

/** 小程序业务单据：管理员发起服务费，用户购买证书并支付自己的账单。 */
@RestController
@RequestMapping("/api/mini/business")
public class MiniBusinessController extends BaseController
{
    @Resource private IZjudgBusinessPaymentService service;
    @Resource private SysUserMapper userMapper;

    @GetMapping("/recipients")
    public AjaxResult recipients(String keyword)
    {
        if (!canManageBusiness()) return AjaxResult.error("仅小程序管理员可以选择付款人");
        if (keyword == null || keyword.trim().length() < 2) return AjaxResult.error("请输入至少两个字符");
        return success(userMapper.selectMiniRecipients(keyword.trim()));
    }

    @GetMapping("/certificates")
    public AjaxResult products() { return success(service.listProducts()); }

    @PostMapping("/certificates/applications")
    public AjaxResult apply(@Valid @RequestBody CertificateBody body)
    {
        return success(service.createCertificateApplication(body.productId(), SecurityUtils.getUserId(), body.phone(), body.photoUrl(), body.address()));
    }

    @GetMapping("/certificates/applications/{id}")
    public AjaxResult application(@PathVariable String id) { return success(service.getApplicationForUser(id, SecurityUtils.getUserId())); }

    @PostMapping("/fees")
    public AjaxResult createFee(@Valid @RequestBody FeeBody body)
    {
        if (!canManageBusiness()) return AjaxResult.error("仅小程序管理员可以发起服务费");
        return success(service.createFee(body.payerUserId(), body.title(), body.amount(), body.remark(), SecurityUtils.getUserId()));
    }

    @GetMapping("/fees/{id}")
    public AjaxResult fee(@PathVariable String id, String shareToken)
    {
        String userId = SecurityUtils.getUserIdIfAuthenticated();
        // A shared link is the access key for the read-only bill view.  It must
        // continue to work when the recipient already happens to be logged in
        // with a different account; payment itself still validates the payer.
        if (shareToken != null && !shareToken.isBlank()) {
            return success(service.getFeeForShare(id, shareToken));
        }
        return success(userId == null ? null : service.getFeeForUser(id, userId));
    }

    @GetMapping("/fees/mine")
    public AjaxResult myFees()
    {
        return success(service.listFeesForUser(SecurityUtils.getUserId()));
    }

    @PostMapping("/payments")
    public AjaxResult createPayment(@Valid @RequestBody PaymentBody body)
    {
        return success(service.createPayment(body.bizType(), body.bizId(), SecurityUtils.getUserId()));
    }

    @GetMapping("/payments/{bizType}/{bizId}/status")
    public AjaxResult paymentStatus(@PathVariable String bizType, @PathVariable String bizId)
    {
        return success(service.syncPayment(bizType, bizId, SecurityUtils.getUserId()));
    }

    private boolean canManageBusiness()
    {
        return SecurityUtils.hasRole("admin") || SecurityUtils.hasRole("mini_admin");
    }

    public record FeeBody(@NotBlank String payerUserId, @NotBlank @Size(max = 128) String title,
            @NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount,
            @Size(max = 500) String remark) { }
    public record CertificateBody(@NotBlank String productId,
            @NotBlank @Pattern(regexp = "^[+0-9() -]{6,32}$") String phone,
            @NotBlank @Size(max = 500) String photoUrl, @NotBlank @Size(max = 255) String address) { }
    public record PaymentBody(@NotBlank String bizType, @NotBlank String bizId) { }
}
