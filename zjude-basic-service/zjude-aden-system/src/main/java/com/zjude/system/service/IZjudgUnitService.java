package com.zjude.system.service;
import java.util.List;
import com.zjude.system.domain.ZjudgServiceUnit;
public interface IZjudgUnitService { List<ZjudgServiceUnit> selectList(ZjudgServiceUnit q); ZjudgServiceUnit selectById(String id); int insert(ZjudgServiceUnit item); int update(ZjudgServiceUnit item); int deleteByIds(String[] ids); }
