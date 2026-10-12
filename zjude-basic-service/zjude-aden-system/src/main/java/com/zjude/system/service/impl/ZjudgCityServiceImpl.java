package com.zjude.system.service.impl;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.system.domain.ZjudgCity;
import com.zjude.system.mapper.ZjudgCityMapper;
import com.zjude.system.service.IZjudgCityService;

@Service("zjudgCityService")
public class ZjudgCityServiceImpl implements IZjudgCityService {
    @Resource private ZjudgCityMapper mapper;
    @Override public List<ZjudgCity> selectList(ZjudgCity query) { return mapper.selectList(query); }
}
