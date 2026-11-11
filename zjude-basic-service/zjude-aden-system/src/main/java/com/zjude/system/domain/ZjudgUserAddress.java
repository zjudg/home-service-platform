package com.zjude.system.domain;

import java.io.Serial;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.zjude.common.core.domain.BaseEntity;

/**
 * 用户服务地址。
 *
 * <p>一个用户可以有多条地址，但同一时刻最多只能有一条 isDefault = "1"，
 * 该约束由 {@code ZjudgUserAddressServiceImpl} 在事务内保证。</p>
 */
public class ZjudgUserAddress extends BaseEntity
{
    @Serial
    private static final long serialVersionUID = 1L;

    private String addressId;
    private String userId;
    private String contactName;
    private String contactPhone;
    private String cityId;
    private String region;
    private String detail;
    private String label;
    private String isDefault;
    private String status;

    public String getAddressId() { return addressId; }
    public void setAddressId(String value) { addressId = value; }

    @NotBlank(message = "联系人不能为空")
    @Size(max = 64, message = "联系人长度不能超过 64 个字符")
    public String getContactName() { return contactName; }
    public void setContactName(String value) { contactName = value; }

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "联系电话格式不正确")
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String value) { contactPhone = value; }

    public String getCityId() { return cityId; }
    public void setCityId(String value) { cityId = value; }

    @Size(max = 255, message = "所在区域长度不能超过 255 个字符")
    public String getRegion() { return region; }
    public void setRegion(String value) { region = value; }

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255, message = "详细地址长度不能超过 255 个字符")
    public String getDetail() { return detail; }
    public void setDetail(String value) { detail = value; }

    @Size(max = 32, message = "地址标签长度不能超过 32 个字符")
    public String getLabel() { return label; }
    public void setLabel(String value) { label = value; }

    public String getIsDefault() { return isDefault; }
    public void setIsDefault(String value) { isDefault = value; }

    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }

    public String getUserId() { return userId; }
    public void setUserId(String value) { userId = value; }

    /** 拼接后可直接写入订单 address 字段的完整地址。 */
    public String getFullAddress()
    {
        StringBuilder builder = new StringBuilder();
        if (region != null)
        {
            builder.append(region.trim());
        }
        if (detail != null)
        {
            builder.append(detail.trim());
        }
        return builder.toString();
    }
}
