package com.zjude.system.mapper;
import java.util.List;
import com.zjude.system.domain.ZjudgService;
public interface ZjudgServiceMapper {
    List<ZjudgService> selectList(ZjudgService query);
    /** 严格城市过滤：只返回属于该城市的服务，不含全国服务 */
    List<ZjudgService> selectListByCity(ZjudgService query);
    ZjudgService selectById(String serviceId);
    ZjudgService selectExtraTimeProduct(@org.apache.ibatis.annotations.Param("cityId") String cityId);
    int countExtraTimeProduct(@org.apache.ibatis.annotations.Param("cityId") String cityId,
            @org.apache.ibatis.annotations.Param("excludeServiceId") String excludeServiceId);
    int insert(ZjudgService item);
    int update(ZjudgService item);
    int deleteByIds(String[] ids);
}
