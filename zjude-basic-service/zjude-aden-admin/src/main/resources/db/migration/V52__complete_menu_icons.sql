-- 补齐后台菜单图标，所有可见目录和菜单使用前端已注册的 SVG 图标。

UPDATE `sys_menu`
SET `icon` = CASE `menu_id`
    WHEN 'M_CFG_PUBLIC_0000000000000000000000000001' THEN 'system'
    WHEN 'M_CFG_PUBLIC_0000000000000000000000000002' THEN 'system'
    WHEN 'M_CFG_PUBLIC_0000000000000000000000000003' THEN 'documentation'
    WHEN 'M_CFG_PUBLIC_0000000000000000000000000004' THEN 'component'
    WHEN 'M_CFG_PUBLIC_0000000000000000000000000005' THEN 'wechat'
    WHEN 'M_CFG_PUBLIC_0000000000000000000000000006' THEN 'money'
    WHEN 'M_CFG_PUBLIC_0000000000000000000000000007' THEN 'message'
    WHEN 'M_CFG_PUBLIC_0000000000000000000000000008' THEN 'upload'
    WHEN 'M_CFG_PUBLIC_0000000000000000000000000009' THEN 'message'
    WHEN 'M_CFG_PUBLIC_0000000000000000000000000010' THEN 'link'
    WHEN 'M_CFG_PUBLIC_0000000000000000000000000012' THEN 'international'
    WHEN 'M_CFG_PUBLIC_0000000000000000000000000013' THEN 'international'
    WHEN 'M_CFG_PUBLIC_0000000000000000000000000014' THEN 'example'
    WHEN 'M_content_001' THEN 'documentation'
    WHEN 'M_content_ad_001' THEN 'example'
    WHEN 'M_content_slot_001' THEN 'table'
    WHEN 'M_content_help_001' THEN 'question'
    WHEN 'M_content_help_category_001' THEN 'tree'
    WHEN 'M_content_article_001' THEN 'documentation'
    WHEN 'M_content_article_category_001' THEN 'list'
    WHEN 'M_decoration_001' THEN 'edit'
    WHEN 'M_decoration_home_001' THEN 'dashboard'
    WHEN 'M_decoration_profile_001' THEN 'user'
    WHEN 'M_decoration_category_001' THEN 'tree'
    WHEN 'M_decoration_tabbar_001' THEN 'tab'
    WHEN 'M_channel_001' THEN 'link'
    WHEN 'M_channel_wechat_001' THEN 'wechat'
    WHEN 'M_channel_menu_001' THEN 'list'
    WHEN 'M_channel_reply_001' THEN 'message'
    WHEN 'M_channel_mini_001' THEN 'wechat'
    WHEN 'M_channel_upload_001' THEN 'upload'
    WHEN 'M_channel_open_001' THEN 'international'
    WHEN 'M_channel_h5_001' THEN 'phone'
    WHEN 'M_channel_pc_001' THEN 'monitor'
    ELSE `icon`
END
WHERE `menu_id` IN (
    'M_CFG_PUBLIC_0000000000000000000000000001',
    'M_CFG_PUBLIC_0000000000000000000000000002',
    'M_CFG_PUBLIC_0000000000000000000000000003',
    'M_CFG_PUBLIC_0000000000000000000000000004',
    'M_CFG_PUBLIC_0000000000000000000000000005',
    'M_CFG_PUBLIC_0000000000000000000000000006',
    'M_CFG_PUBLIC_0000000000000000000000000007',
    'M_CFG_PUBLIC_0000000000000000000000000008',
    'M_CFG_PUBLIC_0000000000000000000000000009',
    'M_CFG_PUBLIC_0000000000000000000000000010',
    'M_CFG_PUBLIC_0000000000000000000000000012',
    'M_CFG_PUBLIC_0000000000000000000000000013',
    'M_CFG_PUBLIC_0000000000000000000000000014',
    'M_content_001', 'M_content_ad_001', 'M_content_slot_001',
    'M_content_help_001', 'M_content_help_category_001',
    'M_content_article_001', 'M_content_article_category_001',
    'M_decoration_001', 'M_decoration_home_001', 'M_decoration_profile_001',
    'M_decoration_category_001', 'M_decoration_tabbar_001',
    'M_channel_001', 'M_channel_wechat_001', 'M_channel_menu_001',
    'M_channel_reply_001', 'M_channel_mini_001', 'M_channel_upload_001',
    'M_channel_open_001', 'M_channel_h5_001', 'M_channel_pc_001'
);
