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
    /** 适用城市ID，为空表示全国 */
    private String cityId;
    private Integer maxReserve;
    private Integer sortNum;
    private String status;
    /** 指定日期下的剩余可预约数，仅小程序查询时填充 */
    private Integer availableCount;
    public String getSlotId() { return slotId; }
    public void setSlotId(String value) { slotId = value; }
    @NotBlank(message = "时段名称不能为空") public String getSlotName() { return slotName; }
    public void setSlotName(String value) { slotName = value; }
    @NotNull(message = "开始时间不能为空") public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime value) { startTime = value; }
    @NotNull(message = "结束时间不能为空") public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime value) { endTime = value; }
    public String getCityId() { return cityId; }
    public void setCityId(String value) { cityId = value; }
    @NotNull(message = "最大预约数不能为空") public Integer getMaxReserve() { return maxReserve; }
    public void setMaxReserve(Integer value) { maxReserve = value; }
    @NotNull(message = "排序不能为空") public Integer getSortNum() { return sortNum; }
    public void setSortNum(Integer value) { sortNum = value; }
    @NotBlank(message = "状态不能为空") public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public Integer getAvailableCount() { return availableCount; }
    public void setAvailableCount(Integer value) { availableCount = value; }
}
