package com.zjude.system.service.impl;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.exception.ServiceException;
import com.zjude.system.domain.ZjudgCity;
import com.zjude.system.mapper.ZjudgCityMapper;
import com.zjude.system.service.IZjudgCityService;

@Service("zjudgCityService")
public class ZjudgCityServiceImpl implements IZjudgCityService {
    @Resource private ZjudgCityMapper mapper;

    @Override public List<ZjudgCity> selectList(ZjudgCity query) { return mapper.selectList(query); }

    @Override public ZjudgCity selectById(String cityId) { return mapper.selectById(cityId); }

    @Override public int countReferences(String[] cityIds) { return mapper.countReferences(cityIds); }

    @Override public int insert(ZjudgCity city) { return mapper.insert(city); }

    @Override public int update(ZjudgCity city) { return mapper.update(city); }

    /**
     * 删除城市。
     *
     * <p>师傅、服务、订单、预约时段都持有 city_id，物理删除会让这些记录的城市名称变成 NULL，
     * 派单按城市匹配的分支也会失效。因此先统计引用，有引用直接拒绝，提示改用停用。</p>
     *
     * @param cityIds 城市ID数组
     * @return 删除行数
     */
    @Override public int deleteByIds(String[] cityIds) {
        if (cityIds == null || cityIds.length == 0) {
            return 0;
        }
        if (mapper.countReferences(cityIds) > 0) {
            throw new ServiceException("选中的城市已被师傅、服务或订单引用，不能删除，请改为停用");
        }
        return mapper.deleteByIds(cityIds);
    }
}
