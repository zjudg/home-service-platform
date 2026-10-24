package com.zjude.system.mapper;
import java.util.List;
import com.zjude.system.domain.ZjudgService;
public interface ZjudgServiceMapper {
    List<ZjudgService> selectList(ZjudgService query);
    /** 严格城市过滤：只返回属于该城市的服务，不含全国服务 */
    List<ZjudgService> selectListByCity(ZjudgService query);
    ZjudgService selectById(String serviceId);
    int insert(ZjudgService item);
    int update(ZjudgService item);
    int deleteByIds(String[] ids);
}
