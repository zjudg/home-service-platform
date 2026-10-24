package com.zjude.system.service.impl;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.utils.StringUtils;
import com.zjude.system.domain.ZjudgBookingSlot;
import com.zjude.system.domain.ZjudgService;
import com.zjude.system.mapper.ZjudgBookingSlotMapper;
import com.zjude.system.mapper.ZjudgServiceMapper;
import com.zjude.system.service.ISysConfigService;
import com.zjude.system.service.IZjudgCityScopeService;

/**
 * 城市维度查询的范围控制。
 *
 * <p>Mapper 中的城市条件统一是「城市为空 or 等于当前城市」，即宽松范围；
 * 严格模式下由本类追加「只返回当前城市」的查询分支，避免把开关判断散落在 SQL 里。</p>
 */
@Service("zjudgCityScopeService")
public class ZjudgCityScopeServiceImpl implements IZjudgCityScopeService
{
    /** 严格城市过滤开关 */
    private static final String CONFIG_STRICT = "zjudg.city.strict";

    @Resource
    private ZjudgServiceMapper serviceMapper;

    @Resource
    private ZjudgBookingSlotMapper slotMapper;

    @Resource
    private ISysConfigService configService;

    @Override
    public boolean strictFilterEnabled()
    {
        return "1".equals(conf(CONFIG_STRICT));
    }

    @Override
    public List<ZjudgService> selectServices(ZjudgService query)
    {
        if (query == null || StringUtils.isEmpty(query.getCityId()))
        {
            return serviceMapper.selectList(query);
        }
        if (strictFilterEnabled())
        {
            return serviceMapper.selectListByCity(query);
        }
        return serviceMapper.selectList(query);
    }

    @Override
    public List<ZjudgBookingSlot> selectSlots(ZjudgBookingSlot query)
    {
        if (query == null || StringUtils.isEmpty(query.getCityId()))
        {
            return slotMapper.selectList(query);
        }
        if (strictFilterEnabled())
        {
            return slotMapper.selectListByCity(query);
        }
        return slotMapper.selectList(query);
    }

    private String conf(String key)
    {
        String value = configService.selectConfigByKey(key);
        return value == null ? "" : value.trim();
    }
}
