package com.zjude.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zjude.system.domain.ZjudgMaterialGroup;

/**
 * 素材分组数据访问。
 */
public interface ZjudgMaterialGroupMapper
{
    List<ZjudgMaterialGroup> selectAll();

    ZjudgMaterialGroup selectById(@Param("groupId") String groupId);

    ZjudgMaterialGroup selectByName(@Param("groupName") String groupName);

    int insert(ZjudgMaterialGroup item);

    int update(ZjudgMaterialGroup item);

    int deleteById(@Param("groupId") String groupId);
}
