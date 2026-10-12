package com.zjude.system.mapper;
import java.util.List;
import com.zjude.system.domain.ZjudgService;
public interface ZjudgServiceMapper {
    List<ZjudgService> selectList(ZjudgService query);
    ZjudgService selectById(String serviceId);
    int insert(ZjudgService item);
    int update(ZjudgService item);
    int deleteByIds(String[] ids);
}
