-- 公共配置功能统一数据库脚本：内容、装修、渠道。

-- 内容管理
CREATE TABLE IF NOT EXISTS `mall_content_item` (
  `content_id` varchar(64) NOT NULL COMMENT '内容资源ID',
  `resource_type` varchar(24) NOT NULL COMMENT '资源类型：AD广告 SLOT广告位 HELP_CAT帮助分类 ARTICLE_CAT文章分类 HELP帮助 ARTICLE文章',
  `title` varchar(200) DEFAULT NULL COMMENT '标题或分类名称',
  `category_id` varchar(64) DEFAULT NULL COMMENT '所属分类ID',
  `slot_id` varchar(64) DEFAULT NULL COMMENT '广告位ID',
  `platform` varchar(16) NOT NULL DEFAULT 'MOBILE' COMMENT '终端：MOBILE移动端 PC电脑端',
  `image` varchar(500) DEFAULT NULL COMMENT '图片地址',
  `link_type` varchar(24) DEFAULT NULL COMMENT '链接类型',
  `link` varchar(1000) DEFAULT NULL COMMENT '跳转链接',
  `intro` varchar(1000) DEFAULT NULL COMMENT '简介',
  `content` longtext COMMENT '正文内容',
  `is_announcement` char(1) NOT NULL DEFAULT '0' COMMENT '是否公告：1是，0否',
  `status` char(1) NOT NULL DEFAULT '1' COMMENT '状态：1启用，0停用',
  `sort_num` int NOT NULL DEFAULT 50 COMMENT '排序',
  `image_width` int DEFAULT NULL COMMENT '建议图片宽度',
  `image_height` int DEFAULT NULL COMMENT '建议图片高度',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞量',
  `create_by` varchar(64) DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '', `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`content_id`),
  KEY `idx_content_type_status_sort` (`resource_type`, `status`, `sort_num`, `create_time`),
  KEY `idx_content_category` (`resource_type`, `category_id`, `status`),
  KEY `idx_content_slot` (`slot_id`, `status`),
  KEY `idx_content_title` (`resource_type`, `title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公共内容管理表';

-- 装修配置支持长文本序列化数据
ALTER TABLE `sys_config`
MODIFY COLUMN `config_value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '参数键值';

-- 渠道配置
CREATE TABLE IF NOT EXISTS `channel_wechat_menu` (
  `menu_id` varchar(64) NOT NULL COMMENT '菜单ID', `parent_id` varchar(64) NOT NULL DEFAULT '0' COMMENT '父菜单ID',
  `menu_name` varchar(64) NOT NULL COMMENT '菜单名称', `menu_type` varchar(24) NOT NULL DEFAULT 'KEYWORD' COMMENT '菜单类型',
  `keyword` varchar(255) DEFAULT NULL COMMENT '关键字', `link` varchar(1000) DEFAULT NULL COMMENT '链接地址',
  `sort_num` int NOT NULL DEFAULT 0 COMMENT '排序', `status` char(1) NOT NULL DEFAULT '1' COMMENT '状态',
  `create_by` varchar(64) DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '', `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`menu_id`), KEY `idx_channel_menu_parent_status` (`parent_id`,`status`,`sort_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='微信公众号菜单';

CREATE TABLE IF NOT EXISTS `channel_wechat_reply_rule` (
  `reply_id` varchar(64) NOT NULL COMMENT '回复规则ID', `rule_name` varchar(128) NOT NULL COMMENT '规则名称',
  `reply_type` varchar(24) NOT NULL DEFAULT 'TEXT' COMMENT '回复类型', `reply_content` longtext NOT NULL COMMENT '回复内容',
  `match_type` varchar(24) NOT NULL DEFAULT 'KEYWORD' COMMENT '匹配类型', `keywords` varchar(1000) DEFAULT NULL COMMENT '关键词',
  `sort_num` int NOT NULL DEFAULT 0 COMMENT '排序', `status` char(1) NOT NULL DEFAULT '1' COMMENT '状态',
  `create_by` varchar(64) DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '', `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`reply_id`), KEY `idx_channel_reply_status_sort` (`status`,`sort_num`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='微信公众号自动回复规则';

CREATE TABLE IF NOT EXISTS `channel_mini_upload_record` (
  `upload_id` varchar(64) NOT NULL COMMENT '上传记录ID', `version` varchar(64) NOT NULL COMMENT '版本号',
  `project_remark` varchar(500) DEFAULT NULL COMMENT '项目备注', `status` varchar(24) NOT NULL DEFAULT 'PENDING' COMMENT '上传状态',
  `operator` varchar(64) DEFAULT NULL COMMENT '操作人', `create_by` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP, `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`upload_id`), KEY `idx_channel_upload_status_time` (`status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='小程序代码上传记录';

-- 管理菜单
INSERT IGNORE INTO `sys_menu` (`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`query`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`remark`) VALUES
('M_content_001','内容','_',12,'mall/content',NULL,NULL,1,0,'M','0','0','','documentation','admin',CURRENT_TIMESTAMP,'公共内容配置'),
('M_content_ad_001','广告管理','M_content_001',1,'advertisement','mall/content/advertisement/index',NULL,1,0,'C','0','0','settings:content:list,settings:content:add,settings:content:edit,settings:content:delete','#','admin',CURRENT_TIMESTAMP,'内容-广告管理'),
('M_content_slot_001','广告位','M_content_001',2,'slot','mall/content/slot/index',NULL,1,0,'C','0','0','settings:content:list,settings:content:add,settings:content:edit,settings:content:delete','#','admin',CURRENT_TIMESTAMP,'内容-广告位'),
('M_content_help_001','帮助管理','M_content_001',3,'help','mall/content/help/index',NULL,1,0,'C','0','0','settings:content:list,settings:content:add,settings:content:edit,settings:content:delete','#','admin',CURRENT_TIMESTAMP,'内容-帮助管理'),
('M_content_help_category_001','帮助分类','M_content_001',4,'help-category','mall/content/help-category/index',NULL,1,0,'C','0','0','settings:content:list,settings:content:add,settings:content:edit,settings:content:delete','#','admin',CURRENT_TIMESTAMP,'内容-帮助分类'),
('M_content_article_001','文章管理','M_content_001',5,'article','mall/content/article/index',NULL,1,0,'C','0','0','settings:content:list,settings:content:add,settings:content:edit,settings:content:delete','#','admin',CURRENT_TIMESTAMP,'内容-文章管理'),
('M_content_article_category_001','文章分类','M_content_001',6,'article-category','mall/content/article-category/index',NULL,1,0,'C','0','0','settings:content:list,settings:content:add,settings:content:edit,settings:content:delete','#','admin',CURRENT_TIMESTAMP,'内容-文章分类'),
('M_decoration_001','装修','_',13,'mall/decoration',NULL,NULL,1,0,'M','0','0','','edit','admin',CURRENT_TIMESTAMP,'公共页面装修'),
('M_decoration_home_001','首页','M_decoration_001',1,'home','mall/decoration/home/index',NULL,1,0,'C','0','0','settings:decorate-home:list,settings:decorate-home:edit','#','admin',CURRENT_TIMESTAMP,'装修-首页'),
('M_decoration_profile_001','我的','M_decoration_001',2,'profile','mall/decoration/profile/index',NULL,1,0,'C','0','0','settings:decorate-profile:list,settings:decorate-profile:edit','#','admin',CURRENT_TIMESTAMP,'装修-我的'),
('M_decoration_category_001','分类','M_decoration_001',3,'category','mall/decoration/category/index',NULL,1,0,'C','0','0','settings:decorate-category:list,settings:decorate-category:edit','#','admin',CURRENT_TIMESTAMP,'装修-分类'),
('M_decoration_tabbar_001','底部导航','M_decoration_001',4,'tabbar','mall/decoration/tabbar/index',NULL,1,0,'C','0','0','settings:decorate-tabbar:list,settings:decorate-tabbar:edit','#','admin',CURRENT_TIMESTAMP,'装修-底部导航'),
('M_channel_001','渠道','_',14,'channel',NULL,NULL,1,0,'M','0','0','','share','admin',CURRENT_TIMESTAMP,'公共渠道配置'),
('M_channel_wechat_001','公众号设置','M_channel_001',1,'wechat','mall/channel/wechat/index',NULL,1,0,'C','0','0','settings:channel-wechat:list,settings:channel-wechat:edit','#','admin',CURRENT_TIMESTAMP,'渠道-公众号设置'),
('M_channel_menu_001','菜单管理','M_channel_001',2,'menu','mall/channel/menu/index',NULL,1,0,'C','0','0','channel:menu:list,channel:menu:query,channel:menu:add,channel:menu:edit,channel:menu:remove','#','admin',CURRENT_TIMESTAMP,'渠道-菜单管理'),
('M_channel_reply_001','回复管理','M_channel_001',3,'reply','mall/channel/reply/index',NULL,1,0,'C','0','0','channel:reply:list,channel:reply:query,channel:reply:add,channel:reply:edit,channel:reply:remove','#','admin',CURRENT_TIMESTAMP,'渠道-回复管理'),
('M_channel_mini_001','小程序设置','M_channel_001',4,'mini','mall/channel/mini/index',NULL,1,0,'C','0','0','settings:channel-mini:list,settings:channel-mini:edit','#','admin',CURRENT_TIMESTAMP,'渠道-小程序设置'),
('M_channel_upload_001','一键上传','M_channel_001',5,'mini-upload','mall/channel/mini-upload/index',NULL,1,0,'C','0','0','settings:channel-mini-upload:list,settings:channel-mini-upload:edit,channel:upload:list,channel:upload:add','#','admin',CURRENT_TIMESTAMP,'渠道-一键上传'),
('M_channel_open_001','微信开放平台','M_channel_001',6,'open-platform',NULL,NULL,1,0,'M','0','0','','share','admin',CURRENT_TIMESTAMP,'渠道-微信开放平台'),
('M_channel_open_settings_001','开放平台设置','M_channel_open_001',1,'settings','mall/channel/open/index',NULL,1,0,'C','0','0','settings:channel-open:list,settings:channel-open:edit','#','admin',CURRENT_TIMESTAMP,'渠道-开放平台设置'),
('M_channel_h5_001','H5商城设置','M_channel_001',8,'h5','mall/channel/h5/index',NULL,1,0,'C','0','0','settings:channel-h5:list,settings:channel-h5:edit','#','admin',CURRENT_TIMESTAMP,'渠道-H5商城设置'),
('M_channel_pc_001','PC商城设置','M_channel_001',9,'pc','mall/channel/pc/index',NULL,1,0,'C','0','0','settings:channel-pc:list,settings:channel-pc:edit','#','admin',CURRENT_TIMESTAMP,'渠道-PC商城设置');

INSERT IGNORE INTO `sys_role_menu` (`role_id`,`menu_id`) SELECT 'ROLE_ADMIN_001', `menu_id` FROM `sys_menu` WHERE `menu_id` LIKE 'M_content_%' OR `menu_id` LIKE 'M_decoration_%' OR `menu_id` LIKE 'M_channel_%';
