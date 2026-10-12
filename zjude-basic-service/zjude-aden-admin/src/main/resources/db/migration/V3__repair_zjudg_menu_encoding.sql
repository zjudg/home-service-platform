-- 修复家政菜单名称的历史字符集写入问题。
UPDATE sys_menu SET menu_name = '服务管理' WHERE menu_id = 'M_ZJUDG_SERVICE';
UPDATE sys_menu SET menu_name = '服务列表' WHERE menu_id = 'M_ZJUDG_SERVICE_LIST';
UPDATE sys_menu SET menu_name = '服务分类' WHERE menu_id = 'M_ZJUDG_CATEGORY';
UPDATE sys_menu SET menu_name = '服务评价' WHERE menu_id = 'M_ZJUDG_REVIEW';
UPDATE sys_menu SET menu_name = '服务单位' WHERE menu_id = 'M_ZJUDG_UNIT';
UPDATE sys_menu SET menu_name = '订单管理' WHERE menu_id = 'M_ZJUDG_ORDER';
UPDATE sys_menu SET menu_name = '订单列表' WHERE menu_id = 'M_ZJUDG_ORDER_LIST';
UPDATE sys_menu SET menu_name = '预约时间段' WHERE menu_id = 'M_ZJUDG_BOOKING';
