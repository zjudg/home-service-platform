package com.zjude.system.service;

import java.util.List;
import java.util.Map;
import com.zjude.system.domain.ZjudgMaterial;

/**
 * 素材库应用服务。
 *
 * <p>删除分组前必须把素材迁移走或清空，避免出现挂在已删除分组下的孤立素材。</p>
 */
public interface IZjudgMaterialService
{
    /**
     * 素材列表。
     *
     * @param query 查询条件
     * @return 素材列表
     */
    List<ZjudgMaterial> list(ZjudgMaterial query);

    /**
     * 素材详情，不存在返回 null。
     *
     * @param materialId 素材主键
     * @return 素材
     */
    ZjudgMaterial detail(String materialId);

    /**
     * 新增素材。
     *
     * @param material 素材
     * @param username 操作人
     * @return 素材主键
     */
    String add(ZjudgMaterial material, String username);

    /**
     * 修改素材。
     *
     * @param material 素材
     * @param username 操作人
     * @return 影响行数
     */
    int update(ZjudgMaterial material, String username);

    /**
     * 批量删除素材。
     *
     * @param materialIds 素材主键集合
     * @return 影响行数
     */
    int remove(List<String> materialIds);

    /**
     * 批量移动素材到指定分组。
     *
     * @param materialIds 素材主键集合
     * @param groupId 目标分组
     * @return 影响行数
     */
    int move(List<String> materialIds, String groupId);

    /**
     * 分组列表，附带每个分组的素材数量。
     *
     * @return 每项包含分组字段与 count
     */
    List<Map<String, Object>> groups();

    /**
     * 新增分组。
     *
     * @param groupName 分组名称
     * @return 分组主键
     */
    String addGroup(String groupName);

    /**
     * 重命名分组。
     *
     * @param groupId 分组主键
     * @param groupName 新名称
     * @return 影响行数
     */
    int renameGroup(String groupId, String groupName);

    /**
     * 删除分组，组内素材会迁移到「未分组」。
     *
     * @param groupId 分组主键
     * @return 迁移的素材数量
     */
    int removeGroup(String groupId);
}
