package com.zjude.system.mapper;

import java.util.List;
import com.zjude.system.domain.SysRoleDept;

/**
 * 角色与部门关联表 数据�? * 
 * @author bai
 */
public interface SysRoleDeptMapper
{
    /**
     * 通过角色ID删除角色和部门关�?     * 
     * @param roleId 角色ID
     * @return 结果
     */
    int deleteRoleDeptByRoleId(String roleId);

    /**
     * 批量删除角色部门关联信息
     * 
     * @param roleIds 需要删除的数据ID
     * @return 结果
     */
    int deleteRoleDept(String[] roleIds);

    /**
     * 查询部门使用数量
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    int selectCountRoleDeptByDeptId(Long deptId);

    /**
     * 批量新增角色部门信息
     * 
     * @param roleDeptList 角色部门列表
     * @return 结果
     */
    int batchRoleDept(List<SysRoleDept> roleDeptList);
}
