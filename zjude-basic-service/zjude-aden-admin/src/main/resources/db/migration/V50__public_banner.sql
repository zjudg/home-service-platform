-- ----------------------------
-- 公共 Banner 内容表和字典。
-- ----------------------------

-- ----------------------------
-- Table structure for mall_banner
-- ----------------------------
CREATE TABLE IF NOT EXISTS `mall_banner`  (
  `seq_no` int NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `banner_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '轮播图ID',
  `banner_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '轮播图标题',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '图片地址',
  `link_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '跳转类型（0无跳转 1内部页面 2外部链接）',
  `link_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '跳转地址',
  `banner_position` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'index' COMMENT '广告位置（index首页 category分类页 activity活动页）',
  `banner_sort` int NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `start_time` datetime NULL DEFAULT NULL COMMENT '生效开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '生效结束时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`seq_no`) USING BTREE,
  UNIQUE INDEX `idx_banner_id`(`banner_id` ASC) USING BTREE COMMENT '业务主键索引',
  INDEX `idx_mall_banner_bp`(`banner_position` ASC) USING BTREE,
  INDEX `idx_mall_banner_s`(`status` ASC) USING BTREE,
  INDEX `idx_mall_banner_se`(`start_time` ASC, `end_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '轮播图表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type`
(`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '轮播图跳转类型', 'mall_banner_link_type', '0', 'admin', CURRENT_TIMESTAMP, '轮播图跳转类型列表'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'mall_banner_link_type');
INSERT INTO `sys_dict_type`
(`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '轮播图广告位置', 'mall_banner_position', '0', 'admin', CURRENT_TIMESTAMP, '轮播图广告位置列表'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'mall_banner_position');

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data`
(`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 1, '无跳转', '0', 'mall_banner_link_type', '', 'info', 'Y', '0', 'admin', CURRENT_TIMESTAMP, '无跳转'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'mall_banner_link_type' AND `dict_value` = '0');
INSERT INTO `sys_dict_data`
(`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 2, '内部页面', '1', 'mall_banner_link_type', '', 'primary', 'N', '0', 'admin', CURRENT_TIMESTAMP, '内部页面'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'mall_banner_link_type' AND `dict_value` = '1');
INSERT INTO `sys_dict_data`
(`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 3, '外部链接', '2', 'mall_banner_link_type', '', 'warning', 'N', '0', 'admin', CURRENT_TIMESTAMP, '外部链接'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'mall_banner_link_type' AND `dict_value` = '2');
INSERT INTO `sys_dict_data`
(`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 1, '首页', 'index', 'mall_banner_position', '', 'primary', 'Y', '0', 'admin', CURRENT_TIMESTAMP, '首页轮播'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'mall_banner_position' AND `dict_value` = 'index');
INSERT INTO `sys_dict_data`
(`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 2, '分类页', 'category', 'mall_banner_position', '', 'success', 'N', '0', 'admin', CURRENT_TIMESTAMP, '分类页轮播'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'mall_banner_position' AND `dict_value` = 'category');
INSERT INTO `sys_dict_data`
(`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 3, '活动页', 'activity', 'mall_banner_position', '', 'warning', 'N', '0', 'admin', CURRENT_TIMESTAMP, '活动页轮播'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'mall_banner_position' AND `dict_value` = 'activity');
