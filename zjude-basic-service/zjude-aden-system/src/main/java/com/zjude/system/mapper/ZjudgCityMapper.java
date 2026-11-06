package com.zjude.system.mapper;

import java.util.List;
import com.zjude.system.domain.ZjudgCity;

public interface ZjudgCityMapper {
    List<ZjudgCity> selectList(ZjudgCity query);

    ZjudgCity selectById(String cityId);

    /**
     * 统计这些城市被业务数据引用的行数。
     *
     * <p>师傅、服务、订单、预约时段都持有 city_id，删除前必须先确认没有引用，
     * 否则这些列表的城市名称会变成 NULL。</p>
     *
     * @param cityIds 城市ID数组
     * @return 引用行数合计
     */
    int countReferences(String[] cityIds);

    int insert(ZjudgCity city);

    int update(ZjudgCity city);

    int deleteByIds(String[] cityIds);
}
