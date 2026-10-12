-- 家政业务基础数据模型。系统功能表保留 sys_ 前缀，家政业务表统一使用 zjudg_ 前缀。
CREATE TABLE IF NOT EXISTS zjudg_service_category (
    category_id varchar(64) NOT NULL COMMENT '分类ID',
    parent_id varchar(64) NOT NULL DEFAULT '0' COMMENT '父分类ID',
    category_name varchar(64) NOT NULL COMMENT '分类名称',
    image_url varchar(500) DEFAULT NULL COMMENT '分类图片',
    sort_num int NOT NULL DEFAULT 0 COMMENT '排序',
    home_recommend char(1) NOT NULL DEFAULT '0' COMMENT '首页推荐（0否 1是）',
    status char(1) NOT NULL DEFAULT '0' COMMENT '状态（0显示 1停用）',
    create_by varchar(64) DEFAULT '' COMMENT '创建者',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    update_by varchar(64) DEFAULT '' COMMENT '更新者',
    update_time datetime DEFAULT NULL COMMENT '更新时间',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (category_id),
    KEY idx_zjudg_category_parent_status (parent_id, status, sort_num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政服务分类';

CREATE TABLE IF NOT EXISTS zjudg_service_unit (
    unit_id varchar(64) NOT NULL COMMENT '单位ID',
    unit_name varchar(32) NOT NULL COMMENT '单位名称',
    sort_num int NOT NULL DEFAULT 0 COMMENT '排序',
    status char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    create_by varchar(64) DEFAULT '' COMMENT '创建者',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    update_by varchar(64) DEFAULT '' COMMENT '更新者',
    update_time datetime DEFAULT NULL COMMENT '更新时间',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (unit_id),
    UNIQUE KEY uk_zjudg_unit_name (unit_name),
    KEY idx_zjudg_unit_status_sort (status, sort_num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政服务单位';

CREATE TABLE IF NOT EXISTS zjudg_service (
    service_id varchar(64) NOT NULL COMMENT '服务ID',
    service_name varchar(128) NOT NULL COMMENT '服务名称',
    service_intro varchar(500) DEFAULT NULL COMMENT '服务简介',
    category_id varchar(64) NOT NULL COMMENT '服务分类ID',
    carousel_images text COMMENT '轮播图地址，逗号分隔',
    price decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '销售价格',
    original_price decimal(12,2) DEFAULT NULL COMMENT '划线价格',
    unit_id varchar(64) NOT NULL COMMENT '服务单位ID',
    sort_num int NOT NULL DEFAULT 0 COMMENT '排序',
    status char(1) NOT NULL DEFAULT '1' COMMENT '服务状态（0上架 1仓库）',
    detail_content longtext COMMENT '服务详情',
    create_by varchar(64) DEFAULT '' COMMENT '创建者',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    update_by varchar(64) DEFAULT '' COMMENT '更新者',
    update_time datetime DEFAULT NULL COMMENT '更新时间',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (service_id),
    KEY idx_zjudg_service_category_status (category_id, status, sort_num),
    KEY idx_zjudg_service_unit (unit_id),
    KEY idx_zjudg_service_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政服务';

CREATE TABLE IF NOT EXISTS zjudg_service_review (
    review_id varchar(64) NOT NULL COMMENT '评价ID',
    service_id varchar(64) NOT NULL COMMENT '服务ID',
    user_id varchar(64) DEFAULT NULL COMMENT '用户ID',
    user_info varchar(128) DEFAULT NULL COMMENT '用户信息脱敏展示',
    rating tinyint NOT NULL DEFAULT 5 COMMENT '评分（1-5）',
    content varchar(1000) NOT NULL COMMENT '评价内容',
    reply_content varchar(1000) DEFAULT NULL COMMENT '回复内容',
    reply_status char(1) NOT NULL DEFAULT '0' COMMENT '回复状态（0未回复 1已回复）',
    status char(1) NOT NULL DEFAULT '0' COMMENT '状态（0显示 1隐藏）',
    create_by varchar(64) DEFAULT '' COMMENT '创建者',
    create_time datetime DEFAULT NULL COMMENT '评价时间',
    update_by varchar(64) DEFAULT '' COMMENT '更新者',
    update_time datetime DEFAULT NULL COMMENT '更新时间',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (review_id),
    KEY idx_zjudg_review_service_status (service_id, status, create_time),
    KEY idx_zjudg_review_reply (reply_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政服务评价';

CREATE TABLE IF NOT EXISTS zjudg_booking_slot (
    slot_id varchar(64) NOT NULL COMMENT '预约时段ID',
    slot_name varchar(64) NOT NULL COMMENT '时段名称',
    start_time time NOT NULL COMMENT '开始时间',
    end_time time NOT NULL COMMENT '结束时间',
    max_reserve int NOT NULL DEFAULT 0 COMMENT '最大预约数',
    sort_num int NOT NULL DEFAULT 0 COMMENT '排序',
    status char(1) NOT NULL DEFAULT '0' COMMENT '状态（0启用 1停用）',
    create_by varchar(64) DEFAULT '' COMMENT '创建者',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    update_by varchar(64) DEFAULT '' COMMENT '更新者',
    update_time datetime DEFAULT NULL COMMENT '更新时间',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (slot_id),
    UNIQUE KEY uk_zjudg_slot_time (start_time, end_time),
    KEY idx_zjudg_slot_status_sort (status, sort_num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政预约时间段';

CREATE TABLE IF NOT EXISTS zjudg_order (
    order_id varchar(64) NOT NULL COMMENT '订单ID',
    order_no varchar(32) NOT NULL COMMENT '订单编号',
    service_id varchar(64) NOT NULL COMMENT '服务ID',
    service_name varchar(128) NOT NULL COMMENT '服务名称快照',
    user_id varchar(64) DEFAULT NULL COMMENT '用户ID',
    user_info varchar(128) DEFAULT NULL COMMENT '用户信息脱敏展示',
    appointment_date date DEFAULT NULL COMMENT '预约日期',
    slot_id varchar(64) DEFAULT NULL COMMENT '预约时段ID',
    slot_name varchar(64) DEFAULT NULL COMMENT '预约时段快照',
    amount decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '订单金额',
    order_status char(1) NOT NULL DEFAULT '0' COMMENT '订单状态（0待确认 1服务中 2已完成 3已取消）',
    payment_status char(1) NOT NULL DEFAULT '0' COMMENT '支付状态（0未支付 1已支付 2已退款）',
    create_time datetime NOT NULL COMMENT '下单时间',
    update_time datetime DEFAULT NULL COMMENT '更新时间',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_zjudg_order_no (order_no),
    KEY idx_zjudg_order_status_time (order_status, create_time),
    KEY idx_zjudg_order_user_time (user_id, create_time),
    KEY idx_zjudg_order_appointment (appointment_date, slot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政服务订单';

-- 管理员动态菜单。ID 使用业务字符串，避免引入自增业务标识。
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon)
VALUES
('M_ZJUDG_SERVICE', '服务管理', '_', 8, 'zjudg-service', NULL, NULL, '1', '0', 'M', '0', '0', NULL, 'list'),
('M_ZJUDG_SERVICE_LIST', '服务列表', 'M_ZJUDG_SERVICE', 1, 'service', 'zjudg/service/index', NULL, '1', '0', 'C', '0', '0', 'zjudg:service:list', NULL),
('M_ZJUDG_CATEGORY', '服务分类', 'M_ZJUDG_SERVICE', 2, 'category', 'zjudg/category/index', NULL, '1', '0', 'C', '0', '0', 'zjudg:category:list', NULL),
('M_ZJUDG_REVIEW', '服务评价', 'M_ZJUDG_SERVICE', 3, 'review', 'zjudg/review/index', NULL, '1', '0', 'C', '0', '0', 'zjudg:review:list', NULL),
('M_ZJUDG_UNIT', '服务单位', 'M_ZJUDG_SERVICE', 4, 'unit', 'zjudg/unit/index', NULL, '1', '0', 'C', '0', '0', 'zjudg:unit:list', NULL),
('M_ZJUDG_ORDER', '订单管理', '_', 9, 'zjudg-order', NULL, NULL, '1', '0', 'M', '0', '0', NULL, 'shopping'),
('M_ZJUDG_ORDER_LIST', '订单列表', 'M_ZJUDG_ORDER', 1, 'order', 'zjudg/order/index', NULL, '1', '0', 'C', '0', '0', 'zjudg:order:list', NULL),
('M_ZJUDG_BOOKING', '预约时间段', 'M_ZJUDG_ORDER', 2, 'booking', 'zjudg/booking/index', NULL, '1', '0', 'C', '0', '0', 'zjudg:booking:list', NULL);
