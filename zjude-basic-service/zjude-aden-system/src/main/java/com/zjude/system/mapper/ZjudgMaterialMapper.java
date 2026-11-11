package com.zjude.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zjude.system.domain.ZjudgMaterial;

/**
 * 素材数据访问。
 */
public interface ZjudgMaterialMapper
{
    /**
     * 按条件查询素材。
     *
     * @param query 查询条件，支持 groupId / materialType / materialName
     * @return 素材列表
     */
    List<ZjudgMaterial> selectList(ZjudgMaterial query);

    ZjudgMaterial selectById(@Param("materialId") String materialId);

    int insert(ZjudgMaterial item);

    int update(ZjudgMaterial item);

    int deleteByIds(@Param("materialIds") List<String> materialIds);

    /** 批量移动到指定分组。 */
    int moveToGroup(@Param("materialIds") List<String> materialIds, @Param("groupId") String groupId);

    /** 统计某分组下的素材数量，用于阻止删除非空分组。 */
    int countByGroup(@Param("groupId") String groupId);
}
