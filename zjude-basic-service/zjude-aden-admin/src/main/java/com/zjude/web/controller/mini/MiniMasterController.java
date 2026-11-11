package com.zjude.web.controller.mini;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.utils.SecurityUtils;
import com.zjude.common.utils.StringUtils;
import com.zjude.system.domain.ZjudgMaster;
import com.zjude.system.service.IZjudgMasterService;

/**
 * 小程序师傅端入驻与身份接口。
 *
 * <p>登录账号一律从 token 解析，不接受客户端传入 userId，避免通过篡改请求体
 * 操作他人档案。本控制器只提供「入驻 + 我的档案 + 接单开关」，订单相关接口见
 * {@code MiniMasterOrderController}。</p>
 */
@RestController
@RequestMapping("/api/mini/master")
public class MiniMasterController extends BaseController
{
    @Resource
    private IZjudgMasterService masterService;

    /**
     * 当前账号的师傅档案。
     *
     * <p>未入驻时返回 {@code applied=false}，前端据此展示「成为师傅」入口而不是错误。</p>
     */
    @GetMapping("/profile")
    public AjaxResult profile()
    {
        String userId = SecurityUtils.getUserId();
        ZjudgMaster master = masterService.selectByUserId(userId);
        AjaxResult result = AjaxResult.success();
        result.put("applied", master != null);
        result.put("master", master);
        result.put("auditStatus", master == null ? null : master.getAuditStatus());
        result.put("canAcceptOrders", master != null && master.canAcceptOrders());
        return result;
    }

    /**
     * 提交入驻申请。重复提交视为更新资料并重新进入待审核。
     */
    @PostMapping("/apply")
    public AjaxResult apply(@Valid @RequestBody ApplyBody body)
    {
        ZjudgMaster applicant = new ZjudgMaster();
        applicant.setUserId(SecurityUtils.getUserId());
        applicant.setMasterName(body.masterName().trim());
        applicant.setRealName(trimToNull(body.realName()));
        applicant.setIdCardNo(trimToNull(body.idCardNo()));
        applicant.setHealthCertUrl(trimToNull(body.healthCertUrl()));
        applicant.setSkillCertUrl(trimToNull(body.skillCertUrl()));
        applicant.setSkillCategoryIds(trimToNull(body.skillCategoryIds()));
        applicant.setPhone(body.phone().trim());
        applicant.setCityId(trimToNull(body.cityId()));
        applicant.setRegion(trimToNull(body.region()));
        applicant.setAvatarUrl(trimToNull(body.avatarUrl()));
        ZjudgMaster saved = masterService.apply(applicant);
        return AjaxResult.success("申请已提交，请等待平台审核", saved);
    }

    /**
     * 切换接单开关。仅审核通过的师傅可操作。
     */
    @PutMapping("/accepting")
    public AjaxResult switchAccepting(@Valid @RequestBody AcceptingBody body)
    {
        masterService.switchAccepting(SecurityUtils.getUserId(), body.accepting());
        return AjaxResult.success("接单状态已更新");
    }

    private String trimToNull(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** 入驻申请请求体 */
    public record ApplyBody(
            @NotBlank(message = "请填写师傅姓名")
            @Size(max = 64, message = "师傅姓名不能超过64个字符") String masterName,
            @Size(max = 64, message = "真实姓名不能超过64个字符") String realName,
            @Pattern(regexp = "^$|^[0-9Xx]{15,18}$", message = "身份证号格式不正确") String idCardNo,
            @NotBlank(message = "请上传健康证图片") @Size(max = 500, message = "健康证地址不能超过500个字符") String healthCertUrl,
            @Size(max = 500, message = "技能证书地址不能超过500个字符") String skillCertUrl,
            @Size(max = 500, message = "技能分类不能超过500个字符") String skillCategoryIds,
            @NotBlank(message = "请填写联系电话")
            @Pattern(regexp = "^1\\d{10}$", message = "联系电话格式不正确") String phone,
            @Size(max = 16, message = "城市ID过长") String cityId,
            @Size(max = 128, message = "地区不能超过128个字符") String region,
            @Size(max = 500, message = "头像地址不能超过500个字符") String avatarUrl)
    {
    }

    /** 接单开关请求体 */
    public record AcceptingBody(
            @NotBlank(message = "接单开关不能为空")
            @Pattern(regexp = "^[01]$", message = "接单开关取值不合法") String accepting)
    {
    }
}
