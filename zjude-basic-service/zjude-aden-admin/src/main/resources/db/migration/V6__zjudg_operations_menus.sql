-- 家政运营扩展表。系统表使用 sys_，业务表统一使用 zjudg_。
CREATE TABLE IF NOT EXISTS zjudg_user_profile (
    user_id varchar(64) NOT NULL,
    user_nickname varchar(64) DEFAULT NULL,
    account varchar(64) NOT NULL,
    phone varchar(32) DEFAULT NULL,
    avatar_url varchar(500) DEFAULT NULL,
    source varchar(32) NOT NULL DEFAULT 'mini_program',
    status char(1) NOT NULL DEFAULT '0',
    create_time datetime DEFAULT NULL,
    update_time datetime DEFAULT NULL,
    remark varchar(500) DEFAULT NULL,
    PRIMARY KEY (user_id), KEY idx_zjudg_user_phone (phone), KEY idx_zjudg_user_create (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政用户';

CREATE TABLE IF NOT EXISTS zjudg_master (
    master_id varchar(64) NOT NULL,
    user_id varchar(64) DEFAULT NULL,
    master_name varchar(64) NOT NULL,
    account varchar(64) DEFAULT NULL,
    phone varchar(32) DEFAULT NULL,
    region varchar(128) DEFAULT NULL,
    avatar_url varchar(500) DEFAULT NULL,
    status char(1) NOT NULL DEFAULT '0',
    home_recommend char(1) NOT NULL DEFAULT '0',
    create_time datetime DEFAULT NULL,
    update_time datetime DEFAULT NULL,
    remark varchar(500) DEFAULT NULL,
    PRIMARY KEY (master_id), KEY idx_zjudg_master_status (status, home_recommend), KEY idx_zjudg_master_region (region)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政师傅';

CREATE TABLE IF NOT EXISTS zjudg_refund (
    refund_id varchar(64) NOT NULL,
    refund_no varchar(40) NOT NULL,
    order_id varchar(64) DEFAULT NULL,
    order_no varchar(32) DEFAULT NULL,
    buyer_info varchar(128) DEFAULT NULL,
    refund_amount decimal(12,2) NOT NULL DEFAULT 0.00,
    refund_type char(1) NOT NULL DEFAULT '0',
    refund_status char(1) NOT NULL DEFAULT '0',
    create_time datetime DEFAULT NULL,
    update_time datetime DEFAULT NULL,
    remark varchar(500) DEFAULT NULL,
    PRIMARY KEY (refund_id), UNIQUE KEY uk_zjudg_refund_no (refund_no), KEY idx_zjudg_refund_status (refund_status, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政退款记录';

CREATE TABLE IF NOT EXISTS zjudg_material_group (
    group_id varchar(64) NOT NULL,
    group_name varchar(64) NOT NULL,
    sort_num int NOT NULL DEFAULT 0,
    create_time datetime DEFAULT NULL,
    update_time datetime DEFAULT NULL,
    PRIMARY KEY (group_id), UNIQUE KEY uk_zjudg_material_group_name (group_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政素材分组';

CREATE TABLE IF NOT EXISTS zjudg_material (
    material_id varchar(64) NOT NULL,
    group_id varchar(64) DEFAULT NULL,
    material_name varchar(128) NOT NULL,
    material_type char(1) NOT NULL DEFAULT '0',
    material_url varchar(500) NOT NULL,
    file_size bigint DEFAULT NULL,
    create_time datetime DEFAULT NULL,
    update_time datetime DEFAULT NULL,
    PRIMARY KEY (material_id), KEY idx_zjudg_material_group_type (group_id, material_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政素材';

CREATE TABLE IF NOT EXISTS zjudg_decoration_config (
    config_id varchar(64) NOT NULL,
    config_type varchar(32) NOT NULL,
    config_json longtext NOT NULL,
    update_time datetime DEFAULT NULL,
    PRIMARY KEY (config_id), UNIQUE KEY uk_zjudg_decoration_type (config_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政页面装修配置';

CREATE TABLE IF NOT EXISTS zjudg_customer_service_config (
    config_id varchar(64) NOT NULL,
    service_title varchar(128) DEFAULT NULL,
    service_time varchar(128) DEFAULT NULL,
    service_phone varchar(32) DEFAULT NULL,
    qr_code_url varchar(500) DEFAULT NULL,
    update_time datetime DEFAULT NULL,
    PRIMARY KEY (config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政客服配置';

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon) VALUES
('M_ZJUDG_USER', '用户管理', '_', 10, 'zjudg-user', NULL, NULL, '1', '0', 'M', '0', '0', NULL, 'user'),
('M_ZJUDG_USER_LIST', '用户列表', 'M_ZJUDG_USER', 1, 'user', 'zjudg/user/index', NULL, '1', '0', 'C', '0', '0', 'zjudg:user:list', NULL),
('M_ZJUDG_MASTER', '师傅管理', '_', 11, 'zjudg-master', NULL, NULL, '1', '0', 'M', '0', '0', NULL, 'peoples'),
('M_ZJUDG_MASTER_LIST', '师傅列表', 'M_ZJUDG_MASTER', 1, 'master', 'zjudg/master/index', NULL, '1', '0', 'C', '0', '0', 'zjudg:master:list', NULL),
('M_ZJUDG_FINANCE', '财务管理', '_', 12, 'zjudg-finance', NULL, NULL, '1', '0', 'M', '0', '0', NULL, 'money'),
('M_ZJUDG_FINANCE_CENTER', '财务中心', 'M_ZJUDG_FINANCE', 1, 'finance', 'zjudg/finance/index', NULL, '1', '0', 'C', '0', '0', 'zjudg:finance:list', NULL),
('M_ZJUDG_REFUND', '退款记录', 'M_ZJUDG_FINANCE', 2, 'refund', 'zjudg/refund/index', NULL, '1', '0', 'C', '0', '0', 'zjudg:refund:list', NULL),
('M_ZJUDG_MATERIAL', '素材管理', '_', 13, 'zjudg-material', NULL, NULL, '1', '0', 'M', '0', '0', NULL, 'documentation'),
('M_ZJUDG_MATERIAL_CENTER', '素材中心', 'M_ZJUDG_MATERIAL', 1, 'material', 'zjudg/material/index', NULL, '1', '0', 'C', '0', '0', 'zjudg:material:list', NULL),
('M_ZJUDG_DECORATION', '页面装修', '_', 14, 'zjudg-decoration', NULL, NULL, '1', '0', 'M', '0', '0', NULL, 'edit'),
('M_ZJUDG_DECORATION_HOME', '首页装修', 'M_ZJUDG_DECORATION', 1, 'home', 'zjudg/decoration/index', 'scope=home', '1', '0', 'C', '0', '0', 'zjudg:decoration:list', NULL),
('M_ZJUDG_DECORATION_PROFILE', '个人中心', 'M_ZJUDG_DECORATION', 2, 'profile', 'zjudg/decoration/index', 'scope=profile', '1', '0', 'C', '0', '0', 'zjudg:decoration:list', NULL),
('M_ZJUDG_CUSTOMER_SERVICE', '客服设置', 'M_ZJUDG_DECORATION', 3, 'customer-service', 'zjudg/decoration/index', 'scope=customer-service', '1', '0', 'C', '0', '0', 'zjudg:decoration:list', NULL);
