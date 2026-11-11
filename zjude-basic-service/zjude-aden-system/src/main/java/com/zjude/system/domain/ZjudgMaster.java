package com.zjude.system.domain;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zjude.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 家政师傅档案。
 *
 * <p>一条档案通过 {@code userId} 关联一个 {@code sys_user}，唯一约束
 * {@code uk_zjudg_master_user} 保证同一登录账号只有一条师傅档案，这是
 * 「用户端与师傅端共用同一小程序」的账号基础。</p>
 *
 * <p>身份判定同时要求 {@code status = '0'}（账号未停用）与
 * {@code auditStatus = '1'}（审核已通过），二者语义分离：停用是运营处置，
 * 审核是入驻结论。</p>
 *
 * <p>身份证号属于个人敏感信息，落库需加密，且不参与列表查询与日志输出。</p>
 */
public class ZjudgMaster extends BaseEntity
{
    @Serial private static final long serialVersionUID = 1L;

    /** 审核状态：待审核 */
    public static final String AUDIT_PENDING = "0";

    /** 审核状态：已通过 */
    public static final String AUDIT_APPROVED = "1";

    /** 审核状态：已驳回 */
    public static final String AUDIT_REJECTED = "2";

    /** 账号状态：正常 */
    public static final String STATUS_NORMAL = "0";

    /** 账号状态：停用 */
    public static final String STATUS_DISABLED = "1";

    /** 接单开关：停接 */
    public static final String ACCEPTING_OFF = "0";

    /** 接单开关：接单中 */
    public static final String ACCEPTING_ON = "1";

    private String masterId;
    /** 关联的登录账号ID */
    private String userId;
    /** 展示用师傅名 */
    private String masterName;
    /** 真实姓名，仅审核与结算使用 */
    private String realName;
    /**
     * 身份证号。
     *
     * <p>仅接收写入，任何接口都不会以 JSON 形式回显明文；后台审核核验走
     * {@link #getIdCardMasked()} 的脱敏值，避免身份信息通过接口批量导出。</p>
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String idCardNo;
    /** 健康证图片地址 */
    private String healthCertUrl;
    /** 技能证书图片地址 */
    private String skillCertUrl;
    /** 服务技能分类ID，逗号分隔 */
    private String skillCategoryIds;
    /** 服务技能分类名称，供后台列表展示 */
    private String skillNames;
    /** 登录账号名，列表展示用 */
    private String account;
    private String phone;
    /** 服务城市ID，关联 zjudg_city */
    private String cityId;
    /** 服务城市名称，列表展示用 */
    private String cityName;
    /** 自由文本地区，保留兼容旧数据 */
    private String region;
    private String avatarUrl;
    /** 账号状态（0正常 1停用） */
    private String status;
    /** 审核状态（0待审核 1已通过 2已驳回） */
    private String auditStatus;
    /** 审核意见，驳回时必填 */
    private String auditRemark;
    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date auditTime;
    /** 审核人 */
    private String auditBy;
    /** 是否首页推荐（0否 1是） */
    private String homeRecommend;
    /** 服务评分，满分 5.0 */
    private BigDecimal rating;
    /** 累计完成订单数 */
    private Integer orderCount;
    /** 接单开关（0停接 1接单中） */
    private String accepting;

    public String getMasterId() { return masterId; }
    public void setMasterId(String value) { masterId = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { userId = value; }
    @NotBlank(message = "师傅姓名不能为空")
    @Size(max = 64, message = "师傅姓名不能超过64个字符")
    public String getMasterName() { return masterName; }
    public void setMasterName(String value) { masterName = value; }
    @Size(max = 64, message = "真实姓名不能超过64个字符")
    public String getRealName() { return realName; }
    public void setRealName(String value) { realName = value; }
    public String getIdCardNo() { return idCardNo; }
    public void setIdCardNo(String value) { idCardNo = value; }

    /**
     * 身份证号脱敏值，保留前 6 位与后 4 位，供后台审核核验身份。
     *
     * <p>无身份证号时返回 null，长度不足以脱敏时整体以 * 替代，避免泄露片段。</p>
     *
     * @return 脱敏后的身份证号
     */
    public String getIdCardMasked()
    {
        if (idCardNo == null || idCardNo.isBlank())
        {
            return null;
        }
        String value = idCardNo.trim();
        if (value.length() < 11)
        {
            return "****";
        }
        return value.substring(0, 6) + "********" + value.substring(value.length() - 4);
    }
    @Size(max = 500, message = "健康证地址不能超过500个字符")
    public String getHealthCertUrl() { return healthCertUrl; }
    public void setHealthCertUrl(String value) { healthCertUrl = value; }
    @Size(max = 500, message = "技能证书地址不能超过500个字符")
    public String getSkillCertUrl() { return skillCertUrl; }
    public void setSkillCertUrl(String value) { skillCertUrl = value; }
    @Size(max = 500, message = "技能分类不能超过500个字符")
    public String getSkillCategoryIds() { return skillCategoryIds; }
    public void setSkillCategoryIds(String value) { skillCategoryIds = value; }
    public String getSkillNames() { return skillNames; }
    public void setSkillNames(String value) { skillNames = value; }
    public String getAccount() { return account; }
    public void setAccount(String value) { account = value; }
    @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确")
    public String getPhone() { return phone; }
    public void setPhone(String value) { phone = value; }
    public String getCityId() { return cityId; }
    public void setCityId(String value) { cityId = value; }
    public String getCityName() { return cityName; }
    public void setCityName(String value) { cityName = value; }
    public String getRegion() { return region; }
    public void setRegion(String value) { region = value; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String value) { avatarUrl = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String value) { auditStatus = value; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String value) { auditRemark = value; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date value) { auditTime = value; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String value) { auditBy = value; }
    public String getHomeRecommend() { return homeRecommend; }
    public void setHomeRecommend(String value) { homeRecommend = value; }
    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal value) { rating = value; }
    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer value) { orderCount = value; }
    public String getAccepting() { return accepting; }
    public void setAccepting(String value) { accepting = value; }

    /** 账号未停用 */
    public boolean isActiveAccount() { return STATUS_NORMAL.equals(status); }

    /** 审核已通过 */
    public boolean isApproved() { return AUDIT_APPROVED.equals(auditStatus); }

    /** 可承接订单：账号正常 + 审核通过 + 接单开关打开 */
    public boolean canAcceptOrders() { return isActiveAccount() && isApproved() && ACCEPTING_ON.equals(accepting); }
}
