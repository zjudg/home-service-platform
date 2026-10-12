CREATE TABLE IF NOT EXISTS zjudg_city (
    city_id varchar(16) NOT NULL COMMENT '行政区划编码',
    city_name varchar(64) NOT NULL COMMENT '城市名称',
    province_name varchar(64) NOT NULL COMMENT '省份名称',
    initial_letter char(1) NOT NULL COMMENT '首字母',
    status char(1) NOT NULL DEFAULT '0' COMMENT '状态（0可用 1停用）',
    create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time datetime DEFAULT NULL,
    PRIMARY KEY (city_id),
    KEY idx_zjudg_city_initial (initial_letter, city_name),
    KEY idx_zjudg_city_province (province_name, city_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政服务城市';

INSERT IGNORE INTO zjudg_service_category (category_id, parent_id, category_name, image_url, sort_num, home_recommend, status, create_by, create_time)
VALUES
('ZC_CLEAN', '0', '保洁清洗', '', 1, '1', '0', 'system', NOW()),
('ZC_REPAIR', '0', '家电维修', '', 2, '1', '0', 'system', NOW()),
('ZC_INSTALL', '0', '上门安装', '', 3, '1', '0', 'system', NOW()),
('ZC_NANNY', '0', '月嫂育儿', '', 4, '1', '0', 'system', NOW()),
('ZC_CARE', '0', '老人陪护', '', 5, '1', '0', 'system', NOW());

INSERT IGNORE INTO zjudg_service_unit (unit_id, unit_name, sort_num, status, create_by, create_time)
VALUES ('ZU_HOUR', '小时', 1, '0', 'system', NOW()), ('ZU_TIME', '次', 2, '0', 'system', NOW()), ('ZU_MONTH', '月', 3, '0', 'system', NOW());

INSERT IGNORE INTO zjudg_service (service_id, service_name, service_intro, category_id, carousel_images, price, original_price, unit_id, sort_num, status, detail_content, create_by, create_time)
VALUES
('ZS_CLEAN_DAILY', '日常保洁', '厨房、卫生间、客厅全屋日常清洁', 'ZC_CLEAN', '', 39.00, 59.00, 'ZU_HOUR', 1, '0', '服务人员按预约时间上门，完成全屋基础清洁。', 'system', NOW()),
('ZS_CLEAN_DEEP', '深度保洁', '针对厨房、卫生间和重点区域的深度清洁', 'ZC_CLEAN', '', 199.00, 259.00, 'ZU_TIME', 2, '0', '专业工具深度处理油污、水垢和卫生死角。', 'system', NOW()),
('ZS_REPAIR_AC', '空调维修', '空调故障检测与上门维修', 'ZC_REPAIR', '', 80.00, 100.00, 'ZU_TIME', 3, '0', '师傅上门检测后报价，维修完成支持服务确认。', 'system', NOW()),
('ZS_INSTALL_WATER', '热水器安装', '热水器及相关配件上门安装', 'ZC_INSTALL', '', 220.00, 280.00, 'ZU_TIME', 4, '0', '按预约时段上门安装并进行安全检查。', 'system', NOW()),
('ZS_NANNY_MATERNITY', '月嫂服务', '持证月嫂提供产后母婴照护', 'ZC_NANNY', '', 6800.00, 7800.00, 'ZU_MONTH', 5, '0', '根据服务周期和护理方案安排专业月嫂。', 'system', NOW()),
('ZS_CARE_ELDER', '老人陪护', '居家老人日常陪护与生活照料', 'ZC_CARE', '', 180.00, 220.00, 'ZU_TIME', 6, '0', '提供生活照料、陪诊及日常陪护服务。', 'system', NOW());

INSERT IGNORE INTO zjudg_decoration_config (config_id, config_type, config_json, update_time)
VALUES ('DEC_HOME_DEFAULT', 'home', '[{"id":"home-clean","title":"安心到家","subtitle":"专业家政服务，提前预约更省心","image":"","serviceId":"ZS_CLEAN_DAILY"}]', NOW());

UPDATE sys_config SET config_value = '妙洁家政', update_time = NOW(), remark = '家政小程序站点名称' WHERE config_key = 'website.shopName';
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '网站名称', 'website.shopName', '妙洁家政', 'N', 'system', NOW(), '家政小程序站点名称'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'website.shopName');
UPDATE sys_config SET config_value = '[{"id":"home-clean","title":"安心到家","subtitle":"专业家政服务，提前预约更省心","image":"","serviceId":"ZS_CLEAN_DAILY"}]', update_time = NOW(), remark = '首页由后台维护的轮播配置' WHERE config_key = 'decorate.home.banners';
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '首页轮播图', 'decorate.home.banners', '[{"id":"home-clean","title":"安心到家","subtitle":"专业家政服务，提前预约更省心","image":"","serviceId":"ZS_CLEAN_DAILY"}]', 'N', 'system', NOW(), '首页由后台维护的轮播配置'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'decorate.home.banners');
UPDATE sys_config SET config_value = '请输入服务名称，如日常保洁', update_time = NOW(), remark = '首页搜索框提示' WHERE config_key = 'decorate.home.searchPlaceholder';
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '首页搜索提示', 'decorate.home.searchPlaceholder', '请输入服务名称，如日常保洁', 'N', 'system', NOW(), '首页搜索框提示'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'decorate.home.searchPlaceholder');
