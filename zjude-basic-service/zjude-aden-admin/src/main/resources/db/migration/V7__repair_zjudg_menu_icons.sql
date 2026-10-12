-- 修复家政菜单使用不存在的图标名称导致一级菜单图标不显示的问题。
-- 图标名称必须与前端 src/assets/icons/svg 下的资源保持一致。
UPDATE sys_menu SET icon = 'list' WHERE menu_id = 'M_ZJUDG_SERVICE';
UPDATE sys_menu SET icon = 'shopping' WHERE menu_id = 'M_ZJUDG_ORDER';
UPDATE sys_menu SET icon = 'user' WHERE menu_id = 'M_ZJUDG_USER';
UPDATE sys_menu SET icon = 'peoples' WHERE menu_id = 'M_ZJUDG_MASTER';
UPDATE sys_menu SET icon = 'money' WHERE menu_id = 'M_ZJUDG_FINANCE';
UPDATE sys_menu SET icon = 'documentation' WHERE menu_id = 'M_ZJUDG_MATERIAL';
UPDATE sys_menu SET icon = 'edit' WHERE menu_id = 'M_ZJUDG_DECORATION';
