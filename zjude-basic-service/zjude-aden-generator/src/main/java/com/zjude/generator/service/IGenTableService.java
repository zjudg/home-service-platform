package com.zjude.generator.service;

import java.util.List;
import java.util.Map;

import com.zjude.generator.domain.GenTable;

/**
 * 业务 服务�? *
 * @author bai
 */
public interface IGenTableService {
    /**
     * 查询业务列表
     *
     * @param genTable 业务信息
     * @return 业务集合
     */
    List<GenTable> selectGenTableList(GenTable genTable);

    /**
     * 查询据库列表
     *
     * @param genTable 业务信息
     * @return 数据库表集合
     */
    List<GenTable> selectDbTableList(GenTable genTable);

    /**
     * 查询据库列表
     *
     * @param tableNames 表名称组
     * @return 数据库表集合
     */
    List<GenTable> selectDbTableListByNames(String[] tableNames);

    /**
     * 查询所有表信息
     *
     * @return 表信息集�?     */
    List<GenTable> selectGenTableAll();

    /**
     * 查询业务信息
     *
     * @param id 业务ID
     * @return 业务信息
     */
    GenTable selectGenTableById(Long id);

    /**
     * 修改业务
     *
     * @param genTable 业务信息
     */
    void updateGenTable(GenTable genTable);

    /**
     * 删除业务信息
     *
     * @param tableIds 需要删除的表数据ID
     */
    void deleteGenTableByIds(Long[] tableIds);

    /**
     * 导入表结�?     *
     * @param tableList 导入表列�?     */
    void importGenTable(List<GenTable> tableList);

    /**
     * 预览代码
     *
     * @param tableId 表编�?     * @return 预览数据列表
     */
    Map<String, String> previewCode(Long tableId);

    /**
     * 生成代码（下载方式）
     *
     * @param tableName 表名�?     * @return 数据
     */
    byte[] downloadCode(String tableName);

    /**
     * 生成代码（自定义路径�?     *
     * @param tableName 表名�?     */
    void generatorCode(String tableName);

    /**
     * 同步数据�?     *
     * @param tableName 表名�?     */
    void syncDb(String tableName);

    /**
     * 批量生成代码（下载方式）
     *
     * @param tableNames 表数�?     * @return 数据
     */
    byte[] downloadCode(String[] tableNames);

    /**
     * 修改保存参数校验
     *
     * @param genTable 业务信息
     */
    void validateEdit(GenTable genTable);
}
