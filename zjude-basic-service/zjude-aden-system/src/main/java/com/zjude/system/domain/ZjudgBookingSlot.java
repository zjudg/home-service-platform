package com.zjude.system.domain;

import java.io.Serial;
import java.time.LocalTime;
import com.zjude.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 家政预约时间段。 */
public class ZjudgBookingSlot extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    private String slotId;
    private String slotName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxReserve;
    private Integer sortNum;
    private String status;
    public String getSlotId() { return slotId; }
    public void setSlotId(String value) { slotId = value; }
    @NotBlank(message = "时段名称不能为空") public String getSlotName() { return slotName; }
    public void setSlotName(String value) { slotName = value; }
    @NotNull(message = "开始时间不能为空") public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime value) { startTime = value; }
    @NotNull(message = "结束时间不能为空") public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime value) { endTime = value; }
    @NotNull(message = "最大预约数不能为空") public Integer getMaxReserve() { return maxReserve; }
    public void setMaxReserve(Integer value) { maxReserve = value; }
    @NotNull(message = "排序不能为空") public Integer getSortNum() { return sortNum; }
    public void setSortNum(Integer value) { sortNum = value; }
    @NotBlank(message = "状态不能为空") public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
}
