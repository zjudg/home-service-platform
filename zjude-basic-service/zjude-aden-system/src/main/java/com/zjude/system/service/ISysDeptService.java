package com.zjude.system.service;

import java.util.List;
import com.zjude.common.core.domain.TreeSelect;
import com.zjude.common.core.domain.entity.SysDept;

/**
 * 部门管理 服务�? * 
 * @author bai
 */
public interface ISysDeptService
{
    /**
     * 查询部门管理数据
     * 
     * @param dept 部门信息
     * @return 部门信息集合
     */
    List<SysDept> selectDeptList(SysDept dept);

    /**
     * 查询部门树结构信�?     * 
     * @param dept 部门信息
     * @return 部门树信息集�?     */
    List<TreeSelect> selectDeptTreeList(SysDept dept);

    /**
     * 构建前端所需要树结构
     * 
     * @param depts 部门列表
     * @return 树结构列�?     */
    List<SysDept> buildDeptTree(List<SysDept> depts);

    /**
     * 构建前端所需要下拉树结构
     * 
     * @param depts 部门列表
     * @return 下拉树结构列�?     */
    List<TreeSelect> buildDeptTreeSelect(List<SysDept> depts);

    /**
     * 根据角色ID查询部门树信�?     * 
     * @param roleId 角色ID
     * @return 选中部门列表
     */
    List<String> selectDeptListByRoleId(String roleId);

    /**
     * 根据部门ID查询信息
     * 
     * @param deptId 部门ID
     * @return 部门信息
     */
    SysDept selectDeptById(String deptId);

    /**
     * 根据ID查询所有子部门（正常状态）
     * 
     * @param deptId 部门ID
     * @return 子部门数
     */
    int selectNormalChildrenDeptById(String deptId);

    /**
     * 是否存在部门子节�?     * 
     * @param deptId 部门ID
     * @return 结果
     */
    boolean hasChildByDeptId(String deptId);

    /**
     * 查询部门是否存在用户
     * 
     * @param deptId 部门ID
     * @return 结果 true 存在 false 不存�?     */
    boolean checkDeptExistUser(String deptId);

    /**
     * 校验部门名称是否唯一
     * 
     * @param dept 部门信息
     * @return 结果
     */
    boolean checkDeptNameNotUnique(SysDept dept);

    /**
     * 校验部门是否有数据权�?     * 
     * @param deptId 部门id
     */
    void checkDeptDataScope(String deptId);

    /**
     * 新增保存部门信息
     * 
     * @param dept 部门信息
     * @return 结果
     */
    int insertDept(SysDept dept);

    /**
     * 修改保存部门信息
     * 
     * @param dept 部门信息
     * @return 结果
     */
    int updateDept(SysDept dept);

    /**
     * 删除部门管理信息
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    int deleteDeptById(String deptId);
}
