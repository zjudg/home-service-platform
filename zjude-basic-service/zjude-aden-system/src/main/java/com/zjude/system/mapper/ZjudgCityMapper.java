package com.zjude.system.mapper;

import java.util.List;
import com.zjude.system.domain.ZjudgCity;

public interface ZjudgCityMapper {
    List<ZjudgCity> selectList(ZjudgCity query);
}
