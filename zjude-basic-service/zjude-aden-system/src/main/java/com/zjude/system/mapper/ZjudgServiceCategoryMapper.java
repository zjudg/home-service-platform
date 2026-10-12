package com.zjude.system.mapper;
import java.util.List;
import com.zjude.system.domain.ZjudgServiceCategory;
public interface ZjudgServiceCategoryMapper {
    List<ZjudgServiceCategory> selectList(ZjudgServiceCategory query);
    ZjudgServiceCategory selectById(String id);
    int insert(ZjudgServiceCategory item);
    int update(ZjudgServiceCategory item);
    int deleteByIds(String[] ids);
}
