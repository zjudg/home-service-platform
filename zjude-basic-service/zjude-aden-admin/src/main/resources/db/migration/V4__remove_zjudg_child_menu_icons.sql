-- 二级业务菜单不显示图标，图标仅配置在一级菜单。
UPDATE sys_menu SET icon = NULL WHERE menu_id IN (
    'M_ZJUDG_SERVICE_LIST', 'M_ZJUDG_CATEGORY', 'M_ZJUDG_REVIEW', 'M_ZJUDG_UNIT',
    'M_ZJUDG_ORDER_LIST', 'M_ZJUDG_BOOKING'
);
