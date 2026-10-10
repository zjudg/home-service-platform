package com.zjude.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zjude.common.core.domain.entity.SysDept;

/**
 * 部门管理 数据�? * 
 * @author bai
 */
public interface SysDeptMapper
{
    /**
     * 查询部门管理数据
     * 
     * @param dept 部门信息
     * @return 部门信息集合
     */
    List<SysDept> selectDeptList(SysDept dept);

    /**
     * 根据角色ID查询部门树信�?     * 
     * @param roleId 角色ID
     * @param deptCheckStrictly 部门树选择项是否关联显�?     * @return 选中部门列表
     */
    List<String> selectDeptListByRoleId(@Param("roleId") String roleId, @Param("deptCheckStrictly") boolean deptCheckStrictly);

    /**
     * 根据部门ID查询信息
     * 
     * @param deptId 部门ID
     * @return 部门信息
     */
    SysDept selectDeptById(String deptId);

    /**
     * 根据ID查询所有子部门
     * 
     * @param deptId 部门ID
     * @return 部门列表
     */
    List<SysDept> selectChildrenDeptById(String deptId);

    /**
     * 根据ID查询所有子部门（正常状态）
     * 
     * @param deptId 部门ID
     * @return 子部门数
     */
    int selectNormalChildrenDeptById(String deptId);

    /**
     * 是否存在子节�?     * 
     * @param deptId 部门ID
     * @return 结果
     */
    int hasChildByDeptId(String deptId);

    /**
     * 查询部门是否存在用户
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    int checkDeptExistUser(String deptId);

    /**
     * 校验部门名称是否唯一
     * 
     * @param deptName 部门名称
     * @param parentId 父部门ID
     * @return 结果
     */
    SysDept checkDeptNameUnique(@Param("deptName") String deptName, @Param("parentId") String parentId);

    /**
     * 新增部门信息
     * 
     * @param dept 部门信息
     * @return 结果
     */
    int insertDept(SysDept dept);

    /**
     * 修改部门信息
     * 
     * @param dept 部门信息
     * @return 结果
     */
    int updateDept(SysDept dept);

    /**
     * 修改所在部门正常状�?     * 
     * @param deptIds 部门ID�?     */
    void updateDeptStatusNormal(String[] deptIds);

    /**
     * 修改子元素关�?     * 
     * @param depts 子元�?     * @return 结果
     */
    int updateDeptChildren(@Param("depts") List<SysDept> depts);

    /**
     * 删除部门管理信息
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    int deleteDeptById(String deptId);
}
