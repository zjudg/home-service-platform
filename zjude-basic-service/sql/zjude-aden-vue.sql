/*
 Navicat Premium Data Transfer

 Source Server         : ZJUDE_STUDIO
 Source Server Type    : MySQL
 Source Server Version : 80300 (8.3.0)
 Source Host           : localhost:3306
 Source Schema         : zjude-aden-vue

 Target Server Type    : MySQL
 Target Server Version : 80300 (8.3.0)
 File Encoding         : 65001

 Date: 09/07/2026 11:00:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table`  (
  `table_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `tpl_web_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '前端模板类型（element-ui模版 element-plus模版）',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gen_table
-- ----------------------------

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column`  (
  `column_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint NULL DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表字段' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gen_table_column
-- ----------------------------

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '参数配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2024-02-04 14:34:43', '', NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO `sys_config` VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2024-02-04 14:34:43', '', NULL, '初始化密码 123456');
INSERT INTO `sys_config` VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2024-02-04 14:34:43', '', NULL, '深色主题theme-dark，浅色主题theme-light');
INSERT INTO `sys_config` VALUES (4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'true', 'Y', 'admin', '2024-02-04 14:34:43', '', NULL, '是否开启验证码功能（true开启，false关闭）');
INSERT INTO `sys_config` VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'admin', '2024-02-04 14:34:43', '', NULL, '是否开启注册用户功能（true开启，false关闭）');
INSERT INTO `sys_config` VALUES (6, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'admin', '2024-02-04 14:34:43', '', NULL, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `seq_no` int NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `dept_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '部门id',
  `parent_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '父部门id',
  `ancestors` varchar(800) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
  `order_num` int NULL DEFAULT 0 COMMENT '显示顺序',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`seq_no`) USING BTREE,
  UNIQUE INDEX `idx_dept_id`(`dept_id` ASC) USING BTREE COMMENT '业务主键索引'
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (1, '100', '_', '_', '小宇科技', 0, '宇', '17888888888', 'yu@qq.com', '0', '0', 'admin', '2024-02-04 14:34:37', '', NULL);
INSERT INTO `sys_dept` VALUES (2, '101', '100', '_,100', '西安总公司', 1, '宇', '17888888888', 'yu@qq.com', '0', '0', 'admin', '2024-02-04 14:34:37', '', NULL);
INSERT INTO `sys_dept` VALUES (3, 'D_66893b7edaf44b44be83accb380189b5', '101', '_,100,101', '研发部门', 1, NULL, NULL, NULL, '0', '0', 'admin', '2024-02-26 16:44:11', '', NULL);
INSERT INTO `sys_dept` VALUES (4, 'D_8d7cc7d4e3994e588f0f449419c376ea', '101', '_,100,101', '测试部门', 2, NULL, NULL, NULL, '0', '0', 'admin', '2024-02-26 16:49:13', '', NULL);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int NULL DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典数据表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '性别男');
INSERT INTO `sys_dict_data` VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '性别女');
INSERT INTO `sys_dict_data` VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '性别未知');
INSERT INTO `sys_dict_data` VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '显示菜单');
INSERT INTO `sys_dict_data` VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '隐藏菜单');
INSERT INTO `sys_dict_data` VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '默认分组');
INSERT INTO `sys_dict_data` VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '系统分组');
INSERT INTO `sys_dict_data` VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '系统默认是');
INSERT INTO `sys_dict_data` VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '系统默认否');
INSERT INTO `sys_dict_data` VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '通知');
INSERT INTO `sys_dict_data` VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '公告');
INSERT INTO `sys_dict_data` VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '关闭状态');
INSERT INTO `sys_dict_data` VALUES (18, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '其他操作');
INSERT INTO `sys_dict_data` VALUES (19, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '新增操作');
INSERT INTO `sys_dict_data` VALUES (20, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '修改操作');
INSERT INTO `sys_dict_data` VALUES (21, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '删除操作');
INSERT INTO `sys_dict_data` VALUES (22, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '授权操作');
INSERT INTO `sys_dict_data` VALUES (23, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '导出操作');
INSERT INTO `sys_dict_data` VALUES (24, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '导入操作');
INSERT INTO `sys_dict_data` VALUES (25, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '强退操作');
INSERT INTO `sys_dict_data` VALUES (26, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '生成操作');
INSERT INTO `sys_dict_data` VALUES (27, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '清空操作');
INSERT INTO `sys_dict_data` VALUES (28, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (29, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2024-02-04 14:34:43', '', NULL, '停用状态');

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2024-02-04 14:34:42', '', NULL, '用户性别列表');
INSERT INTO `sys_dict_type` VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2024-02-04 14:34:42', '', NULL, '菜单状态列表');
INSERT INTO `sys_dict_type` VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2024-02-04 14:34:42', '', NULL, '系统开关列表');
INSERT INTO `sys_dict_type` VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2024-02-04 14:34:42', '', NULL, '任务状态列表');
INSERT INTO `sys_dict_type` VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2024-02-04 14:34:42', '', NULL, '任务分组列表');
INSERT INTO `sys_dict_type` VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2024-02-04 14:34:42', '', NULL, '系统是否列表');
INSERT INTO `sys_dict_type` VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2024-02-04 14:34:42', '', NULL, '通知类型列表');
INSERT INTO `sys_dict_type` VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2024-02-04 14:34:42', '', NULL, '通知状态列表');
INSERT INTO `sys_dict_type` VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2024-02-04 14:34:42', '', NULL, '操作类型列表');
INSERT INTO `sys_dict_type` VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2024-02-04 14:34:42', '', NULL, '登录状态列表');

-- ----------------------------
-- Table structure for sys_login_info
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_info`;
CREATE TABLE `sys_login_info`  (
  `info_id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作系统',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '提示消息',
  `login_time` datetime NULL DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`) USING BTREE,
  INDEX `idx_sys_login_info_s`(`status` ASC) USING BTREE,
  INDEX `idx_sys_login_info_lt`(`login_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统访问记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_login_info
-- ----------------------------
INSERT INTO `sys_login_info` VALUES (1, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-02-29 11:35:20');
INSERT INTO `sys_login_info` VALUES (2, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-01 10:36:24');
INSERT INTO `sys_login_info` VALUES (3, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-01 12:07:49');
INSERT INTO `sys_login_info` VALUES (4, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-01 12:20:34');
INSERT INTO `sys_login_info` VALUES (5, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-01 14:14:52');
INSERT INTO `sys_login_info` VALUES (6, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-01 17:18:34');
INSERT INTO `sys_login_info` VALUES (7, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '退出成功', '2024-03-01 17:19:48');
INSERT INTO `sys_login_info` VALUES (8, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-01 17:47:48');
INSERT INTO `sys_login_info` VALUES (9, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-04 10:45:24');
INSERT INTO `sys_login_info` VALUES (10, 'admin', '10.0.0.2', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-05 11:12:59');
INSERT INTO `sys_login_info` VALUES (11, 'admin', '10.0.0.2', '内网IP', 'Chrome 12', 'Windows 10', '0', '退出成功', '2024-03-05 11:48:36');
INSERT INTO `sys_login_info` VALUES (12, 'admin', '10.0.0.2', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-05 11:53:19');
INSERT INTO `sys_login_info` VALUES (13, 'admin', '10.0.0.2', '内网IP', 'Chrome 12', 'Windows 10', '0', '退出成功', '2024-03-05 12:04:38');
INSERT INTO `sys_login_info` VALUES (14, 'admin', '10.0.0.2', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-05 12:04:45');
INSERT INTO `sys_login_info` VALUES (15, 'admin', '10.0.0.2', '内网IP', 'Chrome 12', 'Windows 10', '0', '退出成功', '2024-03-05 12:07:26');
INSERT INTO `sys_login_info` VALUES (16, 'admin', '10.0.0.2', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-05 12:14:52');
INSERT INTO `sys_login_info` VALUES (17, 'admin', '10.0.0.2', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-05 14:32:21');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `seq_no` int NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `menu_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `parent_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '父菜单ID',
  `order_num` int NULL DEFAULT 0 COMMENT '显示顺序',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由参数',
  `is_frame` int NULL DEFAULT 1 COMMENT '是否为外链（0是 1否）',
  `is_cache` int NULL DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`seq_no`) USING BTREE,
  UNIQUE INDEX `idx_menu_id`(`menu_id` ASC) USING BTREE COMMENT '业务主键索引'
) ENGINE = InnoDB AUTO_INCREMENT = 83 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 'M_f68d6b6392424a0ca8657dc497932360', '系统管理', '_', 1, 'system', NULL, '', 1, 0, 'M', '0', '0', '', 'system', 'admin', '2024-02-04 14:34:40', '', NULL, '系统管理目录');
INSERT INTO `sys_menu` VALUES (2, 'M_2aca6479059240eaa217067c545e3553', '系统监控', '_', 2, 'monitor', NULL, '', 1, 0, 'M', '1', '0', '', 'monitor', 'admin', '2024-02-04 14:34:40', 'admin', '2024-02-27 16:24:58', '系统监控目录');
INSERT INTO `sys_menu` VALUES (3, 'M_4b92f8dc6b1d4c42b8586e9c51b61dd3', '系统工具', '_', 3, 'tool', NULL, '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', '2024-02-04 14:34:40', '', NULL, '系统工具目录');
INSERT INTO `sys_menu` VALUES (4, 'M_3786d7c9d3fa46a5b711f82c0ef2e4b7', '用户管理', 'M_f68d6b6392424a0ca8657dc497932360', 1, 'user', 'system/user/index', '', 1, 0, 'C', '0', '0', 'system:user:list', 'user', 'admin', '2024-02-04 14:34:40', 'admin', '2024-02-27 16:53:17', '用户管理菜单');
INSERT INTO `sys_menu` VALUES (5, 'M_e6e3477f580c46b29827e041be72cbd4', '角色管理', 'M_f68d6b6392424a0ca8657dc497932360', 2, 'role', 'system/role/index', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples', 'admin', '2024-02-04 14:34:40', 'admin', '2024-02-27 16:53:12', '角色管理菜单');
INSERT INTO `sys_menu` VALUES (6, 'M_b88fc14e18c2492eba961bd6dadc0571', '菜单管理', 'M_f68d6b6392424a0ca8657dc497932360', 3, 'menu', 'system/menu/index', '', 1, 0, 'C', '0', '0', 'system:menu:list', 'tree-table', 'admin', '2024-02-04 14:34:40', '', NULL, '菜单管理菜单');
INSERT INTO `sys_menu` VALUES (7, 'M_77e43fd6172343aaa94817625bd2170d', '部门管理', 'M_f68d6b6392424a0ca8657dc497932360', 4, 'dept', 'system/dept/index', '', 1, 0, 'C', '0', '0', 'system:dept:list', 'tree', 'admin', '2024-02-04 14:34:40', '', NULL, '部门管理菜单');
INSERT INTO `sys_menu` VALUES (8, 'M_661ab2afedcd410ea22cf9683d3b955a', '岗位管理', 'M_f68d6b6392424a0ca8657dc497932360', 5, 'post', 'system/post/index', '', 1, 0, 'C', '0', '0', 'system:post:list', 'post', 'admin', '2024-02-04 14:34:40', '', NULL, '岗位管理菜单');
INSERT INTO `sys_menu` VALUES (9, 'M_06b6a908a4fb4c5fa0ffa5650da67989', '字典管理', 'M_f68d6b6392424a0ca8657dc497932360', 6, 'dict', 'system/dict/index', '', 1, 0, 'C', '1', '0', 'system:dict:list', 'dict', 'admin', '2024-02-04 14:34:40', '', NULL, '字典管理菜单');
INSERT INTO `sys_menu` VALUES (10, 'M_5c3ccd4013c9443b938fc339dc89fde4', '参数设置', 'M_f68d6b6392424a0ca8657dc497932360', 7, 'config', 'system/config/index', '', 1, 0, 'C', '1', '0', 'system:config:list', 'edit', 'admin', '2024-02-04 14:34:40', '', NULL, '参数设置菜单');
INSERT INTO `sys_menu` VALUES (11, 'M_f38005a73e224da6ac17a092622406ea', '通知公告', 'M_f68d6b6392424a0ca8657dc497932360', 8, 'notice', 'system/notice/index', '', 1, 0, 'C', '1', '0', 'system:notice:list', 'message', 'admin', '2024-02-04 14:34:40', '', NULL, '通知公告菜单');
INSERT INTO `sys_menu` VALUES (12, 'M_c76823277abd45458478613ee0215137', '日志管理', 'M_f68d6b6392424a0ca8657dc497932360', 9, 'log', '', '', 1, 0, 'M', '1', '0', '', 'log', 'admin', '2024-02-04 14:34:40', '', NULL, '日志管理菜单');
INSERT INTO `sys_menu` VALUES (13, 'M_8e2eb9b626ad4bcb8e1ea0d6d60c21ac', '在线用户', 'M_2aca6479059240eaa217067c545e3553', 1, 'online', 'monitor/online/index', '', 1, 0, 'C', '0', '0', 'monitor:online:list', 'online', 'admin', '2024-02-04 14:34:40', '', NULL, '在线用户菜单');
INSERT INTO `sys_menu` VALUES (15, 'M_522622c37fb94969ac2893c6d7ffbc1e', '数据监控', 'M_2aca6479059240eaa217067c545e3553', 3, 'druid', 'monitor/druid/index', '', 1, 0, 'C', '0', '0', 'monitor:druid:list', 'druid', 'admin', '2024-02-04 14:34:40', '', NULL, '数据监控菜单');
INSERT INTO `sys_menu` VALUES (16, 'M_cf071cc286014fca8a6bd45f50abdf42', '服务监控', 'M_2aca6479059240eaa217067c545e3553', 4, 'server', 'monitor/server/index', '', 1, 0, 'C', '0', '0', 'monitor:server:list', 'server', 'admin', '2024-02-04 14:34:40', '', NULL, '服务监控菜单');
INSERT INTO `sys_menu` VALUES (17, 'M_4a3c9b795ecd4144aa268c853ebfbf29', '缓存监控', 'M_2aca6479059240eaa217067c545e3553', 5, 'cache', 'monitor/cache/index', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis', 'admin', '2024-02-04 14:34:40', '', NULL, '缓存监控菜单');
INSERT INTO `sys_menu` VALUES (18, 'M_56e1fd892d70442fb5aa608e8614e4e4', '缓存列表', 'M_2aca6479059240eaa217067c545e3553', 6, 'cacheList', 'monitor/cache/list', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis-list', 'admin', '2024-02-04 14:34:40', '', NULL, '缓存列表菜单');
INSERT INTO `sys_menu` VALUES (19, 'M_e5fe96366da44764a3d7775765ea9224', '代码生成', 'M_4b92f8dc6b1d4c42b8586e9c51b61dd3', 1, 'gen', 'tool/gen/index', '', 1, 0, 'C', '0', '0', 'tool:gen:list', 'code', 'admin', '2024-02-04 14:34:40', '', NULL, '代码生成菜单');
INSERT INTO `sys_menu` VALUES (20, 'M_7e3d796005c04d3dbe6afe284a08ffc0', '操作日志', 'M_c76823277abd45458478613ee0215137', 1, 'operlog', 'monitor/operlog/index', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list', 'form', 'admin', '2024-02-04 14:34:40', '', NULL, '操作日志菜单');
INSERT INTO `sys_menu` VALUES (21, 'M_c45c71700a054394ac580c0771e63855', '登录日志', 'M_c76823277abd45458478613ee0215137', 2, 'login-info', 'monitor/login-info/index', '', 1, 0, 'C', '0', '0', 'monitor:login-info:list', 'logininfor', 'admin', '2024-02-04 14:34:40', 'admin', '2024-02-28 16:05:09', '登录日志菜单');
INSERT INTO `sys_menu` VALUES (22, 'M_30665c2b70644df8a184055ed9e238ba', '用户查询', 'M_3786d7c9d3fa46a5b711f82c0ef2e4b7', 1, '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (23, 'M_12916710e260422691f7cf2c84ebc915', '用户新增', 'M_3786d7c9d3fa46a5b711f82c0ef2e4b7', 2, '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (24, 'M_8106621904d24ce285c4b556408a1c42', '用户修改', 'M_3786d7c9d3fa46a5b711f82c0ef2e4b7', 3, '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (25, 'M_61b9e88eb85a4a08bc0b240706ef1fe8', '用户删除', 'M_3786d7c9d3fa46a5b711f82c0ef2e4b7', 4, '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (26, 'M_d5b6cfedb18345f6b24f57a0342e674d', '用户导出', 'M_3786d7c9d3fa46a5b711f82c0ef2e4b7', 5, '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (27, 'M_0c0c4aecfa1a4d57812a72a00ce1d521', '用户导入', 'M_3786d7c9d3fa46a5b711f82c0ef2e4b7', 6, '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (28, 'M_874865c6f46e4758a4a86951a9e43c93', '重置密码', 'M_3786d7c9d3fa46a5b711f82c0ef2e4b7', 7, '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (29, 'M_b67cb3a470484258b258c0896fe952fe', '角色查询', 'M_e6e3477f580c46b29827e041be72cbd4', 1, '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (30, 'M_ef871fe5574346728d44a36d9f39c9e3', '角色新增', 'M_e6e3477f580c46b29827e041be72cbd4', 2, '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (31, 'M_46fda0122f5940e787bf239f5604763e', '角色修改', 'M_e6e3477f580c46b29827e041be72cbd4', 3, '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (32, 'M_f05dcc0c892745918a3dae787722fe20', '角色删除', 'M_e6e3477f580c46b29827e041be72cbd4', 4, '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (33, 'M_cc9b4c95ff2841f38a22e1170c45fd1a', '角色导出', 'M_e6e3477f580c46b29827e041be72cbd4', 5, '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (34, 'M_e3fbf46ede0e438fa8eb58708e2b8e86', '菜单查询', 'M_b88fc14e18c2492eba961bd6dadc0571', 1, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (35, 'M_a5525276220c468794b8b2b137c384d2', '菜单新增', 'M_b88fc14e18c2492eba961bd6dadc0571', 2, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (36, 'M_b42a75225f6e45f688a90d95d12683df', '菜单修改', 'M_b88fc14e18c2492eba961bd6dadc0571', 3, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (37, 'M_91241c46aff74684af6a1ff31fa823e9', '菜单删除', 'M_b88fc14e18c2492eba961bd6dadc0571', 4, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (38, 'M_8632c37632d14f23a91efed530b4269b', '部门查询', 'M_77e43fd6172343aaa94817625bd2170d', 1, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (39, 'M_3a7749777d844d24993743e473e44888', '部门新增', 'M_77e43fd6172343aaa94817625bd2170d', 2, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (40, 'M_ceef855da1a84fb0a7b43bc00f540ee7', '部门修改', 'M_77e43fd6172343aaa94817625bd2170d', 3, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (41, 'M_42380ecb74bd48248ac4c2489a26a34b', '部门删除', 'M_77e43fd6172343aaa94817625bd2170d', 4, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (42, 'M_f60190b3b4fc49fda01e3345799bdde3', '岗位查询', 'M_661ab2afedcd410ea22cf9683d3b955a', 1, '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (43, 'M_c353614dd8db4e4f80c3562fdcfdd48f', '岗位新增', 'M_661ab2afedcd410ea22cf9683d3b955a', 2, '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (44, 'M_1e353522215544a980efd4bc73cbec85', '岗位修改', 'M_661ab2afedcd410ea22cf9683d3b955a', 3, '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (45, 'M_9a6d8f08da86407a99b7181d872845fb', '岗位删除', 'M_661ab2afedcd410ea22cf9683d3b955a', 4, '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (46, 'M_73037793e6504fb4aacb7ffd35ea2136', '岗位导出', 'M_661ab2afedcd410ea22cf9683d3b955a', 5, '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (47, 'M_b0b67c3e4f8f4430ac7c0c3661f22efd', '字典查询', 'M_06b6a908a4fb4c5fa0ffa5650da67989', 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:query', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (48, 'M_5d791b895842421b824cf8be9d413f1c', '字典新增', 'M_06b6a908a4fb4c5fa0ffa5650da67989', 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:add', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (49, 'M_50db456118fe4dbf984b9f4f7e82be2a', '字典修改', 'M_06b6a908a4fb4c5fa0ffa5650da67989', 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (50, 'M_de72d1d24d344a33aca3713345d59d87', '字典删除', 'M_06b6a908a4fb4c5fa0ffa5650da67989', 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (51, 'M_684c7d0c8cd845c586ae9a62d40c22e7', '字典导出', 'M_06b6a908a4fb4c5fa0ffa5650da67989', 5, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:export', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (52, 'M_98f5ef833592456e8493e4686cd8b418', '参数查询', 'M_5c3ccd4013c9443b938fc339dc89fde4', 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:query', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (53, 'M_bf30c49382eb4da789106e3091a661f3', '参数新增', 'M_5c3ccd4013c9443b938fc339dc89fde4', 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:add', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (54, 'M_c40ccbb29f2f4956988ab330dc095972', '参数修改', 'M_5c3ccd4013c9443b938fc339dc89fde4', 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:edit', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (55, 'M_33082d10de6b4cdb8ec443f5e9791018', '参数删除', 'M_5c3ccd4013c9443b938fc339dc89fde4', 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:remove', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (56, 'M_6fa76da5ee0d476191eada42e3509a82', '参数导出', 'M_5c3ccd4013c9443b938fc339dc89fde4', 5, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:export', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (57, 'M_af7051d587cd44f18a613ba3aab9ed18', '公告查询', 'M_f38005a73e224da6ac17a092622406ea', 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (58, 'M_aa7c80d2bd6540378ab0acc1bfcfec0a', '公告新增', 'M_f38005a73e224da6ac17a092622406ea', 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (59, 'M_09d5e42068424a6aac8721eb50689071', '公告修改', 'M_f38005a73e224da6ac17a092622406ea', 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (60, 'M_240cc4ed0743468cb71486ecd3aa7832', '公告删除', 'M_f38005a73e224da6ac17a092622406ea', 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (61, 'M_6ca013d2a12843e98898cbfae9679ddc', '操作查询', 'M_7e3d796005c04d3dbe6afe284a08ffc0', 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (62, 'M_79a73b41afd74ecc87fab183b37043d9', '操作删除', 'M_7e3d796005c04d3dbe6afe284a08ffc0', 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (63, 'M_cbbdbc4bcdc14197b910da0a11f570b0', '日志导出', 'M_7e3d796005c04d3dbe6afe284a08ffc0', 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (64, 'M_de9901ffac6d486dbf4813e6d2e43441', '登录查询', 'M_c45c71700a054394ac580c0771e63855', 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:login-info:query', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (65, 'M_007da3dda0e14dba94703a8f0067e112', '登录删除', 'M_c45c71700a054394ac580c0771e63855', 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:login-info:remove', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (66, 'M_2f4e9ecf07ec477db3409c3dab3af10d', '日志导出', 'M_c45c71700a054394ac580c0771e63855', 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:login-info:export', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (67, 'M_9c90ff6826ad4cd0ad7352e1fd59a7b8', '账户解锁', 'M_c45c71700a054394ac580c0771e63855', 4, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:login-info:unlock', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (68, 'M_0a8115b0704b42308d60460d89f85352', '在线查询', 'M_8e2eb9b626ad4bcb8e1ea0d6d60c21ac', 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (69, 'M_29bac63560ba4262870be2131781f2c4', '批量强退', 'M_8e2eb9b626ad4bcb8e1ea0d6d60c21ac', 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (70, 'M_cae5b56c9cf3494fbc744dd19272807b', '单条强退', 'M_8e2eb9b626ad4bcb8e1ea0d6d60c21ac', 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (77, 'M_42a5f18b199c49cd90b6e280d6938b1c', '生成查询', 'M_e5fe96366da44764a3d7775765ea9224', 1, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (78, 'M_853ac225d1c04fb3b8388bc2a9fa04fd', '生成修改', 'M_e5fe96366da44764a3d7775765ea9224', 2, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (79, 'M_ae4807673fa141d5bda7d5f0ce6f2119', '生成删除', 'M_e5fe96366da44764a3d7775765ea9224', 3, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (80, 'M_6b7d23c7e57f4bffa5310460a2bec520', '导入代码', 'M_e5fe96366da44764a3d7775765ea9224', 4, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (81, 'M_f5b8d0ac05104c32b24aee475b5a6569', '预览代码', 'M_e5fe96366da44764a3d7775765ea9224', 5, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (82, 'M_8ad3391dcf514ef388ec400a1de1e411', '生成代码', 'M_e5fe96366da44764a3d7775765ea9224', 6, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code', '#', 'admin', '2024-02-04 14:34:40', '', NULL, '');

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `notice_id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob NULL COMMENT '公告内容',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES (1, '温馨提醒：2018-07-01 若依新版本发布啦', '2', 0xE696B0E78988E69CACE58685E5AEB9, '0', 'admin', '2024-02-04 14:34:44', '', NULL, '管理员');
INSERT INTO `sys_notice` VALUES (2, '维护通知：2018-07-01 若依系统凌晨维护', '1', 0xE7BBB4E68AA4E58685E5AEB9, '0', 'admin', '2024-02-04 14:34:44', '', NULL, '管理员');

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '模块标题',
  `business_type` int NULL DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求方式',
  `operator_type` int NULL DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '返回参数',
  `status` int NULL DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint NULL DEFAULT 0 COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`) USING BTREE,
  INDEX `idx_sys_oper_log_bt`(`business_type` ASC) USING BTREE,
  INDEX `idx_sys_oper_log_s`(`status` ASC) USING BTREE,
  INDEX `idx_sys_oper_log_ot`(`oper_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO `sys_oper_log` VALUES (1, '菜单管理', 2, 'com.bai.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"system/user/index\",\"createTime\":\"2024-02-04 14:34:40\",\"icon\":\"user\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":\"M_3786d7c9d3fa46a5b711f82c0ef2e4b7\",\"menuName\":\"用户管理\",\"menuType\":\"C\",\"orderNum\":2,\"params\":{},\"parentId\":\"M_f68d6b6392424a0ca8657dc497932360\",\"path\":\"user\",\"perms\":\"system:user:list\",\"query\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2024-02-27 16:24:25', 38);
INSERT INTO `sys_oper_log` VALUES (2, '菜单管理', 2, 'com.bai.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"system/role/index\",\"createTime\":\"2024-02-04 14:34:40\",\"icon\":\"peoples\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":\"M_e6e3477f580c46b29827e041be72cbd4\",\"menuName\":\"角色管理\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":\"M_f68d6b6392424a0ca8657dc497932360\",\"path\":\"role\",\"perms\":\"system:role:list\",\"query\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2024-02-27 16:24:44', 32);
INSERT INTO `sys_oper_log` VALUES (3, '菜单管理', 2, 'com.bai.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2024-02-04 14:34:40\",\"icon\":\"monitor\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":\"M_2aca6479059240eaa217067c545e3553\",\"menuName\":\"系统监控\",\"menuType\":\"M\",\"orderNum\":1,\"params\":{},\"parentId\":\"_\",\"path\":\"monitor\",\"perms\":\"\",\"query\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2024-02-27 16:24:51', 20);
INSERT INTO `sys_oper_log` VALUES (4, '菜单管理', 2, 'com.bai.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2024-02-04 14:34:40\",\"icon\":\"monitor\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":\"M_2aca6479059240eaa217067c545e3553\",\"menuName\":\"系统监控\",\"menuType\":\"M\",\"orderNum\":2,\"params\":{},\"parentId\":\"_\",\"path\":\"monitor\",\"perms\":\"\",\"query\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2024-02-27 16:24:58', 35);
INSERT INTO `sys_oper_log` VALUES (5, '菜单管理', 2, 'com.bai.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"system/role/index\",\"createTime\":\"2024-02-04 14:34:40\",\"icon\":\"peoples\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":\"M_e6e3477f580c46b29827e041be72cbd4\",\"menuName\":\"角色管理\",\"menuType\":\"C\",\"orderNum\":2,\"params\":{},\"parentId\":\"M_f68d6b6392424a0ca8657dc497932360\",\"path\":\"role\",\"perms\":\"system:role:list\",\"query\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2024-02-27 16:53:13', 166);
INSERT INTO `sys_oper_log` VALUES (6, '菜单管理', 2, 'com.bai.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"system/user/index\",\"createTime\":\"2024-02-04 14:34:40\",\"icon\":\"user\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":\"M_3786d7c9d3fa46a5b711f82c0ef2e4b7\",\"menuName\":\"用户管理\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":\"M_f68d6b6392424a0ca8657dc497932360\",\"path\":\"user\",\"perms\":\"system:user:list\",\"query\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2024-02-27 16:53:17', 33);
INSERT INTO `sys_oper_log` VALUES (7, '用户头像', 2, 'com.bai.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '127.0.0.1', '内网IP', '', '{\"msg\":\"操作成功\",\"imgUrl\":\"/profile/avatar/2024/02/27/879677a9532a45ff97259a6149b3b510_20240227171231A001.jpeg\",\"code\":200}', 0, NULL, '2024-02-27 17:12:31', 231);
INSERT INTO `sys_oper_log` VALUES (8, '菜单管理', 2, 'com.bai.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"monitor/login-info/index\",\"createTime\":\"2024-02-04 14:34:40\",\"icon\":\"logininfor\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":\"M_c45c71700a054394ac580c0771e63855\",\"menuName\":\"登录日志\",\"menuType\":\"C\",\"orderNum\":2,\"params\":{},\"parentId\":\"M_c76823277abd45458478613ee0215137\",\"path\":\"login-info\",\"perms\":\"monitor:login-info:list\",\"query\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2024-02-28 16:05:09', 68);
INSERT INTO `sys_oper_log` VALUES (9, '登录日志', 9, 'com.bai.web.controller.monitor.SysLoginInfoController.clean()', 'DELETE', 1, 'admin', '研发部门', '/monitor/login/information/clean', '127.0.0.1', '内网IP', '{}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2024-02-28 16:13:18', 535);
INSERT INTO `sys_oper_log` VALUES (10, '用户头像', 2, 'com.bai.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '10.0.0.2', '内网IP', '', '{\"msg\":\"操作成功\",\"imgUrl\":\"/profile/avatar/2024/03/05/879677a9532a45ff97259a6149b3b510_20240305113105A001.jpeg\",\"code\":200}', 0, NULL, '2024-03-05 11:30:40', 287);
INSERT INTO `sys_oper_log` VALUES (11, '用户管理', 5, 'com.bai.web.controller.system.SysUserController.export()', 'POST', 1, 'admin', '研发部门', '/system/user/export', '10.0.0.2', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', NULL, 0, NULL, '2024-03-05 11:53:26', 901);
INSERT INTO `sys_oper_log` VALUES (12, '角色管理', 5, 'com.bai.web.controller.system.SysRoleController.export()', 'POST', 1, 'admin', '研发部门', '/system/role/export', '10.0.0.2', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', NULL, 0, NULL, '2024-03-05 11:54:07', 83);
INSERT INTO `sys_oper_log` VALUES (13, '角色管理', 5, 'com.bai.web.controller.system.SysRoleController.export()', 'POST', 1, 'admin', '研发部门', '/system/role/export', '10.0.0.2', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', NULL, 0, NULL, '2024-03-05 11:54:25', 74);
INSERT INTO `sys_oper_log` VALUES (14, '岗位管理', 5, 'com.bai.web.controller.system.SysPostController.export()', 'POST', 1, 'admin', '研发部门', '/system/post/export', '10.0.0.2', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', NULL, 0, NULL, '2024-03-05 11:57:17', 76);
INSERT INTO `sys_oper_log` VALUES (15, '岗位管理', 5, 'com.bai.web.controller.system.SysPostController.export()', 'POST', 1, 'admin', '研发部门', '/system/post/export', '10.0.0.2', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', NULL, 0, NULL, '2024-03-05 12:02:35', 1106);
INSERT INTO `sys_oper_log` VALUES (16, '用户管理', 5, 'com.bai.web.controller.system.SysUserController.export()', 'POST', 1, 'admin', '研发部门', '/system/user/export', '10.0.0.2', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', NULL, 0, NULL, '2024-03-05 12:03:13', 115);

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
  `seq_no` int NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `post_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位ID',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int NOT NULL COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`seq_no`) USING BTREE,
  UNIQUE INDEX `idx_post_id`(`post_id` ASC) USING BTREE COMMENT '业务主键索引'
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '岗位信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_post
-- ----------------------------
INSERT INTO `sys_post` VALUES (1, 'P_6873b7edaf44b44he83accb18018b5', 'ceo', '董事长', 1, '0', 'admin', '2024-02-04 14:34:39', '', NULL, '');
INSERT INTO `sys_post` VALUES (2, 'P_c9a978f273cb49c395f19edc8015c774', 'normal', '普通员工', 2, '0', 'admin', '2024-02-26 17:10:21', 'bai', '2024-02-27 15:00:45', NULL);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `seq_no` int NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `role_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色ID',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '部门树选择项是否关联显示',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`seq_no`) USING BTREE,
  UNIQUE INDEX `idx_role_id`(`role_id` ASC) USING BTREE COMMENT '角色主键索引'
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'ROLE_ADMIN_001', '超级管理员', 'admin', 1, '1', 1, 1, '0', '0', 'admin', '2024-02-04 14:34:39', '', NULL, '超级管理员');
INSERT INTO `sys_role` VALUES (2, 'R_8893b7edaf44b44he83accb38018b5', '普通角色', 'common', 2, '1', 1, 1, '0', '0', 'admin', '2024-02-04 14:34:39', 'bai', '2024-02-27 15:28:21', '普通角色');

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
  `role_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色ID',
  `dept_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色和部门关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色ID',
  `menu_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色和菜单关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_007da3dda0e14dba94703a8f0067e112');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_06b6a908a4fb4c5fa0ffa5650da67989');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_09d5e42068424a6aac8721eb50689071');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_0a8115b0704b42308d60460d89f85352');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_0c0c4aecfa1a4d57812a72a00ce1d521');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_12916710e260422691f7cf2c84ebc915');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_1e353522215544a980efd4bc73cbec85');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_2323f948cddb4b5bae9440ce883a7e74');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_240cc4ed0743468cb71486ecd3aa7832');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_29bac63560ba4262870be2131781f2c4');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_2aca6479059240eaa217067c545e3553');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_2f4e9ecf07ec477db3409c3dab3af10d');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_30665c2b70644df8a184055ed9e238ba');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_33082d10de6b4cdb8ec443f5e9791018');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_3786d7c9d3fa46a5b711f82c0ef2e4b7');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_3a7749777d844d24993743e473e44888');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_3ec16349feed469aa8c89169e46a8e57');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_42380ecb74bd48248ac4c2489a26a34b');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_46fda0122f5940e787bf239f5604763e');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_47b59b48d2cf49de99b39192c4f496b9');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_4a3c9b795ecd4144aa268c853ebfbf29');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_50db456118fe4dbf984b9f4f7e82be2a');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_522622c37fb94969ac2893c6d7ffbc1e');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_56e1fd892d70442fb5aa608e8614e4e4');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_5c3ccd4013c9443b938fc339dc89fde4');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_5d791b895842421b824cf8be9d413f1c');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_61b9e88eb85a4a08bc0b240706ef1fe8');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_661ab2afedcd410ea22cf9683d3b955a');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_684c7d0c8cd845c586ae9a62d40c22e7');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_6ca013d2a12843e98898cbfae9679ddc');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_6fa76da5ee0d476191eada42e3509a82');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_73037793e6504fb4aacb7ffd35ea2136');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_77e43fd6172343aaa94817625bd2170d');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_79a73b41afd74ecc87fab183b37043d9');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_7e3d796005c04d3dbe6afe284a08ffc0');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_8106621904d24ce285c4b556408a1c42');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_8632c37632d14f23a91efed530b4269b');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_874865c6f46e4758a4a86951a9e43c93');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_8e2eb9b626ad4bcb8e1ea0d6d60c21ac');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_91241c46aff74684af6a1ff31fa823e9');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_98f5ef833592456e8493e4686cd8b418');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_9a6d8f08da86407a99b7181d872845fb');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_9c90ff6826ad4cd0ad7352e1fd59a7b8');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_a5525276220c468794b8b2b137c384d2');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_aa7c80d2bd6540378ab0acc1bfcfec0a');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_af7051d587cd44f18a613ba3aab9ed18');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_b0b67c3e4f8f4430ac7c0c3661f22efd');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_b42a75225f6e45f688a90d95d12683df');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_b4b0aba279a24b7b924b4e3f20986088');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_b67cb3a470484258b258c0896fe952fe');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_b88fc14e18c2492eba961bd6dadc0571');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_bc9bae804e314729b5931246850376a3');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_bf30c49382eb4da789106e3091a661f3');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_c353614dd8db4e4f80c3562fdcfdd48f');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_c40ccbb29f2f4956988ab330dc095972');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_c45c71700a054394ac580c0771e63855');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_c76823277abd45458478613ee0215137');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_cae5b56c9cf3494fbc744dd19272807b');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_cbbdbc4bcdc14197b910da0a11f570b0');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_cc9b4c95ff2841f38a22e1170c45fd1a');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_ceef855da1a84fb0a7b43bc00f540ee7');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_cf071cc286014fca8a6bd45f50abdf42');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_d5b6cfedb18345f6b24f57a0342e674d');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_d92d6810003f498c95dc81bba59904b7');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_de72d1d24d344a33aca3713345d59d87');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_de9901ffac6d486dbf4813e6d2e43441');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_e3fbf46ede0e438fa8eb58708e2b8e86');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_e6e3477f580c46b29827e041be72cbd4');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_e99807de88a74ffa9a7a5c4b29a74c0a');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_ef871fe5574346728d44a36d9f39c9e3');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_f05dcc0c892745918a3dae787722fe20');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_f38005a73e224da6ac17a092622406ea');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_f60190b3b4fc49fda01e3345799bdde3');
INSERT INTO `sys_role_menu` VALUES ('R_8893b7edaf44b44he83accb38018b5', 'M_f68d6b6392424a0ca8657dc497932360');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `seq_no` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `dept_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户昵称',
  `user_type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '00' COMMENT '用户类型（00系统用户）',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户邮箱',
  `phone_number` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '手机号码',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '密码',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`seq_no`) USING BTREE,
  UNIQUE INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '用户唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'USER_ADMIN_001', 'D_66893b7edaf44b44be83accb380189b5', 'admin', 'admin', '00', 'yu@163.com', '17888888888', '0', '/profile/avatar/2024/03/05/879677a9532a45ff97259a6149b3b510_20240305113105A001.jpeg', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '10.0.0.2', '2024-03-05 14:32:48', 'admin', '2024-02-04 14:34:38', '', '2024-03-05 14:32:21', '管理员');
INSERT INTO `sys_user` VALUES (2, 'U_a2f65ff722bb4b799657a346d6ccb461', 'D_66893b7edaf44b44be83accb380189b5', 'bai', '白', '00', '1750081579@qq.com', '17392662006', '0', '', '$2a$10$sKkyXSvyzi/..2bKKqE./umn0KHxLovt2g3TqstdQU8ZNptZu.Nqu', '0', '0', '127.0.0.1', '2024-02-27 10:32:38', 'admin', '2024-02-26 18:25:54', 'admin', '2024-02-27 16:11:08', '普通用户');

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
  `user_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `post_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`, `post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户与岗位关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
INSERT INTO `sys_user_post` VALUES ('USER_ADMIN_001', 'P_6873b7edaf44b44he83accb18018b5');
INSERT INTO `sys_user_post` VALUES ('U_a2f65ff722bb4b799657a346d6ccb461', 'P_c9a978f273cb49c395f19edc8015c774');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `role_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户和角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES ('USER_ADMIN_001', 'ROLE_ADMIN_001');
INSERT INTO `sys_user_role` VALUES ('U_a2f65ff722bb4b799657a346d6ccb461', 'R_8893b7edaf44b44he83accb38018b5');

SET FOREIGN_KEY_CHECKS = 1;
