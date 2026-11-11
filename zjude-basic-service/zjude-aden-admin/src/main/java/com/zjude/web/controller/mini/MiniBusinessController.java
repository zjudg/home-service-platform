package com.zjude.web.controller.mini;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

/** 小程序业务单据：用户购买证书并支付自己的业务订单。 */
@RestController
@RequestMapping("/api/mini/business")
public class MiniBusinessController extends BaseController
{
    @Resource private IZjudgBusinessPaymentService service;

    @GetMapping("/certificates")
    public AjaxResult products() { return success(service.listProducts()); }

    @PostMapping("/certificates/applications")
    public AjaxResult apply(@Valid @RequestBody CertificateBody body)
    {
        return success(service.createCertificateApplication(body.productId(), SecurityUtils.getUserId(), body.phone(), body.photoUrl(), body.address()));
    }

    @GetMapping("/certificates/applications/{id}")
    public AjaxResult application(@PathVariable String id) { return success(service.getApplicationForUser(id, SecurityUtils.getUserId())); }

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

    public record CertificateBody(@NotBlank String productId,
            @NotBlank @Pattern(regexp = "^[+0-9() -]{6,32}$") String phone,
            @NotBlank @Size(max = 500) String photoUrl, @NotBlank @Size(max = 255) String address) { }
    public record PaymentBody(@NotBlank String bizType, @NotBlank String bizId) { }
}
