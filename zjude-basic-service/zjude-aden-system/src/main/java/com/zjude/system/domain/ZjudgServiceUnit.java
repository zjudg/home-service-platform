package com.zjude.system.domain;

import java.io.Serial;
import com.zjude.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 家政服务单位。 */
public class ZjudgServiceUnit extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    private String unitId;
    private String unitName;
    private Integer sortNum;
    private String status;
    public String getUnitId() { return unitId; }
    public void setUnitId(String value) { unitId = value; }
    @NotBlank(message = "单位名称不能为空") public String getUnitName() { return unitName; }
    public void setUnitName(String value) { unitName = value; }
    @NotNull(message = "排序不能为空") public Integer getSortNum() { return sortNum; }
    public void setSortNum(Integer value) { sortNum = value; }
    @NotBlank(message = "状态不能为空") public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
}
