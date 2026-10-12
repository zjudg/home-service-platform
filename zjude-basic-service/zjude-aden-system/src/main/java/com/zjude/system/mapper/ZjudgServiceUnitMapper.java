package com.zjude.system.mapper;
import java.util.List;
import com.zjude.system.domain.ZjudgServiceUnit;
public interface ZjudgServiceUnitMapper {
    List<ZjudgServiceUnit> selectList(ZjudgServiceUnit query);
    ZjudgServiceUnit selectById(String id);
    int insert(ZjudgServiceUnit item);
    int update(ZjudgServiceUnit item);
    int deleteByIds(String[] ids);
}
