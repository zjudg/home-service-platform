package com.zjude.system.service;

import java.util.List;

import com.zjude.system.domain.ZjudgBookingSlot;
import com.zjude.system.domain.ZjudgService;

/**
 * 小程序侧的城市维度查询：统一处理 {@code zjudg.city.strict} 开关。
 *
 * <p>开关取值来自 {@code sys_config}：</p>
 * <ul>
 *   <li>{@code 0}（默认）：返回「全国 + 当前城市」的数据，某城市尚未配置专属数据时列表不会空白</li>
 *   <li>{@code 1}（严格）：只返回当前城市的数据，全国数据不参与</li>
 * </ul>
 *
 * <p>未传城市时不施加城市条件，行为与严格开关无关。</p>
 */
public interface IZjudgCityScopeService
{
    /**
     * 是否启用严格城市过滤。
     *
     * @return 配置为 1 时返回 true
     */
    boolean strictFilterEnabled();

    /**
     * 按城市范围查询服务列表。
     *
     * @param query 查询条件，cityId 为空时不做城市过滤
     * @return 服务列表
     */
    List<ZjudgService> selectServices(ZjudgService query);

    /**
     * 按城市范围查询预约时段。
     *
     * @param query 查询条件，cityId 为空时不做城市过滤
     * @return 时段列表
     */
    List<ZjudgBookingSlot> selectSlots(ZjudgBookingSlot query);
}
