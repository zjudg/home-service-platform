package com.zjude.system.service;

import java.util.List;
import com.zjude.system.domain.SysPost;

/**
 * 岗位信息 服务�? * 
 * @author bai
 */
public interface ISysPostService
{
    /**
     * 查询岗位信息集合
     * 
     * @param post 岗位信息
     * @return 岗位列表
     */
    List<SysPost> selectPostList(SysPost post);

    /**
     * 查询所有岗�?     * 
     * @return 岗位列表
     */
    List<SysPost> selectPostAll();

    /**
     * 通过岗位ID查询岗位信息
     * 
     * @param postId 岗位ID
     * @return 角色对象信息
     */
    SysPost selectPostById(String postId);

    /**
     * 根据用户ID获取岗位选择框列�?     * 
     * @param userId 用户ID
     * @return 选中岗位ID列表
     */
    List<String> selectPostListByUserId(String userId);

    /**
     * 校验岗位名称
     * 
     * @param post 岗位信息
     * @return 结果
     */
    boolean checkPostNameNotUnique(SysPost post);

    /**
     * 校验岗位编码
     * 
     * @param post 岗位信息
     * @return 结果
     */
    boolean checkPostCodeNotUnique(SysPost post);

    /**
     * 通过岗位ID查询岗位使用数量
     * 
     * @param postId 岗位ID
     * @return 结果
     */
    int countUserPostById(String postId);

    /**
     * 删除岗位信息
     * 
     * @param postId 岗位ID
     * @return 结果
     */
    int deletePostById(String postId);

    /**
     * 批量删除岗位信息
     * 
     * @param postIds 需要删除的岗位ID
     * @return 结果
     */
    int deletePostByIds(String[] postIds);

    /**
     * 新增保存岗位信息
     * 
     * @param post 岗位信息
     * @return 结果
     */
    int insertPost(SysPost post);

    /**
     * 修改保存岗位信息
     * 
     * @param post 岗位信息
     * @return 结果
     */
    int updatePost(SysPost post);
}
