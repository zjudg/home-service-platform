package com.zjude.system.service;

import java.util.List;
import com.zjude.system.domain.ZjudgCity;

public interface IZjudgCityService {
    List<ZjudgCity> selectList(ZjudgCity query);
}
