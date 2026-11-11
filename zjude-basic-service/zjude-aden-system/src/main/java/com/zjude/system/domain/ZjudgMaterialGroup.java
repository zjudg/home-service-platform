package com.zjude.system.domain;

import java.io.Serial;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.zjude.common.core.domain.BaseEntity;

/**
 * 素材分组。
 */
public class ZjudgMaterialGroup extends BaseEntity
{
    @Serial
    private static final long serialVersionUID = 1L;

    /** 未分组固定主键，删除分组时素材回落到这里 */
    public static final String UNGROUPED_ID = "MG_UNGROUPED";

    private String groupId;

    private String groupName;

    private Integer sortNum;

    public String getGroupId() { return groupId; }
    public void setGroupId(String value) { groupId = value; }

    @NotBlank(message = "分组名称不能为空")
    @Size(max = 64, message = "分组名称长度不能超过 64 个字符")
    public String getGroupName() { return groupName; }
    public void setGroupName(String value) { groupName = value; }

    public Integer getSortNum() { return sortNum; }
    public void setSortNum(Integer value) { sortNum = value; }
}
