-- 统一后台菜单分类和排序，保证可见路由最多两级。

-- 一级分类顺序：系统管理、系统工具、公共配置、内容、装修、渠道。
UPDATE `sys_menu`
SET `order_num` = 5
WHERE `menu_id` = 'M_content_001';
UPDATE `sys_menu`
SET `order_num` = 6
WHERE `menu_id` = 'M_decoration_001';
UPDATE `sys_menu`
SET `order_num` = 7
WHERE `menu_id` = 'M_channel_001';

-- 公共配置：地图页面直接归入公共配置，隐藏原地图目录，避免三级菜单。
UPDATE `sys_menu`
SET `parent_id` = 'M_CFG_PUBLIC_0000000000000000000000000001', `order_num` = 10
WHERE `menu_id` = 'M_CFG_PUBLIC_0000000000000000000000000012';
UPDATE `sys_menu`
SET `parent_id` = 'M_CFG_PUBLIC_0000000000000000000000000001', `order_num` = 11
WHERE `menu_id` = 'M_CFG_PUBLIC_0000000000000000000000000013';
UPDATE `sys_menu`
SET `parent_id` = '_', `order_num` = 99, `visible` = '1'
WHERE `menu_id` = 'M_CFG_PUBLIC_0000000000000000000000000011';
UPDATE `sys_menu`
SET `order_num` = 12
WHERE `menu_id` = 'M_CFG_PUBLIC_0000000000000000000000000014';

-- 渠道：开放平台设置直接作为“微信开放平台”菜单，隐藏原二级目录。
UPDATE `sys_menu`
SET `menu_type` = 'C',
    `component` = 'mall/channel/open/index',
    `perms` = 'settings:channel-open:list,settings:channel-open:edit',
    `order_num` = 6
WHERE `menu_id` = 'M_channel_open_001';
UPDATE `sys_menu`
SET `parent_id` = '_', `order_num` = 99, `visible` = '1'
WHERE `menu_id` = 'M_channel_open_settings_001';
UPDATE `sys_menu`
SET `order_num` = 7
WHERE `menu_id` = 'M_channel_h5_001';
UPDATE `sys_menu`
SET `order_num` = 8
WHERE `menu_id` = 'M_channel_pc_001';

-- 系统管理：日志页面直接挂在系统管理下，隐藏原日志目录。
UPDATE `sys_menu`
SET `parent_id` = 'M_f68d6b6392424a0ca8657dc497932360', `order_num` = 10
WHERE `menu_id` = 'M_7e3d796005c04d3dbe6afe284a08ffc0';
UPDATE `sys_menu`
SET `parent_id` = 'M_f68d6b6392424a0ca8657dc497932360', `order_num` = 11
WHERE `menu_id` = 'M_c45c71700a054394ac580c0771e63855';
UPDATE `sys_menu`
SET `parent_id` = '_', `order_num` = 99, `visible` = '1'
WHERE `menu_id` = 'M_c76823277abd45458478613ee0215137';
