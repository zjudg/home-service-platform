package com.zjude.system.service;

import java.util.List;
import com.zjude.system.domain.ZjudgCity;

public interface IZjudgCityService {
    List<ZjudgCity> selectList(ZjudgCity query);

    ZjudgCity selectById(String cityId);

    /**
     * 统计这些城市被业务数据引用的行数。
     *
     * @param cityIds 城市ID数组
     * @return 引用行数合计
     */
    int countReferences(String[] cityIds);

    int insert(ZjudgCity city);

    int update(ZjudgCity city);

    int deleteByIds(String[] cityIds);
}
