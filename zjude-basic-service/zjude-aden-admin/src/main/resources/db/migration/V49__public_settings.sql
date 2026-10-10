-- 公共配置模块初始化。
-- 只包含通用配置、渠道配置和管理端菜单，不包含商品、订单、会员、营销等商城业务。

-- 支付证书、私钥以及部分渠道配置可能超过 500 字符，公共配置统一使用长文本存储。
ALTER TABLE `sys_config`
MODIFY COLUMN `config_value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '参数键值';

INSERT IGNORE INTO `sys_menu`
(`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`,
 `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
 `create_by`, `create_time`, `remark`)
VALUES
('M_CFG_PUBLIC_0000000000000000000000000001', '公共配置', '_', 4, 'settings', NULL, NULL, 1, 0, 'M', '0', '0', '', 'setting', 'admin', CURRENT_TIMESTAMP, '通用业务基础框架配置'),
('M_CFG_PUBLIC_0000000000000000000000000002', '网站设置', 'M_CFG_PUBLIC_0000000000000000000000000001', 1, 'website', 'system/settings/website/index', NULL, 1, 0, 'C', '0', '0', 'settings:website:list', '#', 'admin', CURRENT_TIMESTAMP, '公共配置-网站设置'),
('M_CFG_PUBLIC_0000000000000000000000000003', '版权备案', 'M_CFG_PUBLIC_0000000000000000000000000001', 2, 'copyright/filing', 'system/settings/copyright/filing/index', NULL, 1, 0, 'C', '0', '0', 'settings:copyright:list', '#', 'admin', CURRENT_TIMESTAMP, '公共配置-版权备案'),
('M_CFG_PUBLIC_0000000000000000000000000004', 'APP设置', 'M_CFG_PUBLIC_0000000000000000000000000001', 3, 'app', 'system/settings/app/index', NULL, 1, 0, 'C', '0', '0', 'settings:app:list', '#', 'admin', CURRENT_TIMESTAMP, '公共配置-APP设置'),
('M_CFG_PUBLIC_0000000000000000000000000005', '小程序配置', 'M_CFG_PUBLIC_0000000000000000000000000001', 4, 'mini', 'mall/channel/mini/index', NULL, 1, 0, 'C', '0', '0', 'settings:channel-mini:list', '#', 'admin', CURRENT_TIMESTAMP, '公共配置-小程序配置'),
('M_CFG_PUBLIC_0000000000000000000000000006', '支付设置', 'M_CFG_PUBLIC_0000000000000000000000000001', 5, 'payment', 'system/settings/payment/index', NULL, 1, 0, 'C', '0', '0', 'settings:payment:list', '#', 'admin', CURRENT_TIMESTAMP, '公共配置-支付设置'),
('M_CFG_PUBLIC_0000000000000000000000000007', '短信设置', 'M_CFG_PUBLIC_0000000000000000000000000001', 6, 'sms', 'system/settings/sms/index', NULL, 1, 0, 'C', '0', '0', 'settings:sms:list', '#', 'admin', CURRENT_TIMESTAMP, '公共配置-短信设置'),
('M_CFG_PUBLIC_0000000000000000000000000008', '上传设置', 'M_CFG_PUBLIC_0000000000000000000000000001', 7, 'upload', 'system/settings/upload/index', NULL, 1, 0, 'C', '0', '0', 'settings:upload:list', '#', 'admin', CURRENT_TIMESTAMP, '公共配置-上传设置'),
('M_CFG_PUBLIC_0000000000000000000000000009', '通知设置', 'M_CFG_PUBLIC_0000000000000000000000000001', 8, 'notification', 'system/settings/notification/index', NULL, 1, 0, 'C', '0', '0', 'settings:notification:list', '#', 'admin', CURRENT_TIMESTAMP, '公共配置-通知设置'),
('M_CFG_PUBLIC_0000000000000000000000000010', '分享设置', 'M_CFG_PUBLIC_0000000000000000000000000001', 9, 'share', 'system/settings/share/index', NULL, 1, 0, 'C', '0', '0', 'settings:share:list', '#', 'admin', CURRENT_TIMESTAMP, '公共配置-分享设置'),
('M_CFG_PUBLIC_0000000000000000000000000011', '地图设置', 'M_CFG_PUBLIC_0000000000000000000000000001', 10, 'map', NULL, NULL, 1, 0, 'M', '0', '0', '', 'map-location', 'admin', CURRENT_TIMESTAMP, '公共配置-地图设置'),
('M_CFG_PUBLIC_0000000000000000000000000012', '腾讯地图', 'M_CFG_PUBLIC_0000000000000000000000000011', 1, 'tencent', 'mall/settings/map/tencent/index', NULL, 1, 0, 'C', '0', '0', 'settings:map-tencent:list', '#', 'admin', CURRENT_TIMESTAMP, '公共配置-腾讯地图'),
('M_CFG_PUBLIC_0000000000000000000000000013', '天地图', 'M_CFG_PUBLIC_0000000000000000000000000011', 2, 'tianditu', 'mall/settings/map/tianditu/index', NULL, 1, 0, 'C', '0', '0', 'settings:map-tianditu:list', '#', 'admin', CURRENT_TIMESTAMP, '公共配置-天地图'),
('M_CFG_PUBLIC_0000000000000000000000000014', 'Banner管理', 'M_CFG_PUBLIC_0000000000000000000000000001', 11, 'banner', 'mall/banner/index', NULL, 1, 0, 'C', '0', '0', 'mall:banner:list', 'example', 'admin', CURRENT_TIMESTAMP, '公共配置-Banner管理'),
('M_CFG_PUBLIC_0000000000000000000000000020', '网站设置修改', 'M_CFG_PUBLIC_0000000000000000000000000002', 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'settings:website:edit', '#', 'admin', CURRENT_TIMESTAMP, ''),
('M_CFG_PUBLIC_0000000000000000000000000021', '版权备案修改', 'M_CFG_PUBLIC_0000000000000000000000000003', 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'settings:copyright:edit', '#', 'admin', CURRENT_TIMESTAMP, ''),
('M_CFG_PUBLIC_0000000000000000000000000022', 'APP设置修改', 'M_CFG_PUBLIC_0000000000000000000000000004', 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'settings:app:edit', '#', 'admin', CURRENT_TIMESTAMP, ''),
('M_CFG_PUBLIC_0000000000000000000000000023', '小程序配置修改', 'M_CFG_PUBLIC_0000000000000000000000000005', 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'settings:channel-mini:edit', '#', 'admin', CURRENT_TIMESTAMP, ''),
('M_CFG_PUBLIC_0000000000000000000000000024', '支付设置修改', 'M_CFG_PUBLIC_0000000000000000000000000006', 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'settings:payment:edit', '#', 'admin', CURRENT_TIMESTAMP, ''),
('M_CFG_PUBLIC_0000000000000000000000000025', '短信设置修改', 'M_CFG_PUBLIC_0000000000000000000000000007', 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'settings:sms:edit', '#', 'admin', CURRENT_TIMESTAMP, ''),
('M_CFG_PUBLIC_0000000000000000000000000026', '上传设置修改', 'M_CFG_PUBLIC_0000000000000000000000000008', 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'settings:upload:edit', '#', 'admin', CURRENT_TIMESTAMP, ''),
('M_CFG_PUBLIC_0000000000000000000000000027', '通知设置修改', 'M_CFG_PUBLIC_0000000000000000000000000009', 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'settings:notification:edit', '#', 'admin', CURRENT_TIMESTAMP, ''),
('M_CFG_PUBLIC_0000000000000000000000000028', '分享设置修改', 'M_CFG_PUBLIC_0000000000000000000000000010', 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'settings:share:edit', '#', 'admin', CURRENT_TIMESTAMP, ''),
('M_CFG_PUBLIC_0000000000000000000000000029', '腾讯地图修改', 'M_CFG_PUBLIC_0000000000000000000000000012', 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'settings:map-tencent:edit', '#', 'admin', CURRENT_TIMESTAMP, ''),
('M_CFG_PUBLIC_0000000000000000000000000030', '天地图修改', 'M_CFG_PUBLIC_0000000000000000000000000013', 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'settings:map-tianditu:edit', '#', 'admin', CURRENT_TIMESTAMP, ''),
('M_CFG_PUBLIC_0000000000000000000000000031', 'Banner查询', 'M_CFG_PUBLIC_0000000000000000000000000014', 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'mall:banner:query', '#', 'admin', CURRENT_TIMESTAMP, ''),
('M_CFG_PUBLIC_0000000000000000000000000032', 'Banner新增', 'M_CFG_PUBLIC_0000000000000000000000000014', 2, '', NULL, NULL, 1, 0, 'F', '0', '0', 'mall:banner:add', '#', 'admin', CURRENT_TIMESTAMP, ''),
('M_CFG_PUBLIC_0000000000000000000000000033', 'Banner修改', 'M_CFG_PUBLIC_0000000000000000000000000014', 3, '', NULL, NULL, 1, 0, 'F', '0', '0', 'mall:banner:edit', '#', 'admin', CURRENT_TIMESTAMP, ''),
('M_CFG_PUBLIC_0000000000000000000000000034', 'Banner删除', 'M_CFG_PUBLIC_0000000000000000000000000014', 4, '', NULL, NULL, 1, 0, 'F', '0', '0', 'mall:banner:remove', '#', 'admin', CURRENT_TIMESTAMP, ''),
('M_CFG_PUBLIC_0000000000000000000000000035', 'Banner导出', 'M_CFG_PUBLIC_0000000000000000000000000014', 5, '', NULL, NULL, 1, 0, 'F', '0', '0', 'mall:banner:export', '#', 'admin', CURRENT_TIMESTAMP, '');

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.`role_id`, m.`menu_id`
FROM `sys_role` r
JOIN `sys_menu` m ON m.`menu_id` LIKE 'M_CFG_PUBLIC_%'
WHERE r.`role_key` = 'admin';

-- 配置键只初始化名称和空值，生产环境密钥必须由部署人员在管理端或密钥管理系统中填写。
INSERT INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`)
SELECT s.`config_name`, s.`config_key`, s.`config_value`, 'N', 'admin', CURRENT_TIMESTAMP, s.`remark`
FROM (
    SELECT '网站名称' config_name, 'website.shopName' config_key, '' config_value, '网站设置' remark
    UNION ALL SELECT '移动端Logo', 'website.mobileLogo', '', '网站设置'
    UNION ALL SELECT '默认会员头像', 'website.memberDefaultAvatar', '', '网站设置'
    UNION ALL SELECT '商品默认图片', 'website.goodsDefaultImage', '', '网站设置'
    UNION ALL SELECT '版权信息', 'filing.copyright', '', '版权备案'
    UNION ALL SELECT 'ICP备案号', 'filing.icpNumber', '', '版权备案'
    UNION ALL SELECT '备案链接', 'filing.icpUrl', '', '版权备案'
    UNION ALL SELECT 'APP下载文案', 'app.downloadText', '', 'APP设置'
    UNION ALL SELECT 'iOS下载地址', 'app.iosDownloadUrl', '', 'APP设置'
    UNION ALL SELECT 'Android下载地址', 'app.androidDownloadUrl', '', 'APP设置'
    UNION ALL SELECT '隐私协议开关', 'app.privacyAgreement', '0', 'APP设置'
    UNION ALL SELECT '微信登录开关', 'app.wechatLogin', '0', 'APP设置'
    UNION ALL SELECT '小程序名称', 'channel.mini.name', '', '小程序配置'
    UNION ALL SELECT '小程序AppID', 'channel.mini.appId', '', '小程序配置'
    UNION ALL SELECT '小程序AppSecret', 'channel.mini.appSecret', '', '小程序配置'
    UNION ALL SELECT '小程序码', 'channel.mini.qrcode', '', '小程序配置'
    UNION ALL SELECT '小程序request合法域名', 'channel.mini.requestDomain', '', '小程序配置'
    UNION ALL SELECT '小程序downloadFile合法域名', 'channel.mini.downloadFileDomain', '', '小程序配置'
    UNION ALL SELECT '微信支付开关', 'payment.wechat.status', '0', '支付设置'
    UNION ALL SELECT '微信支付AppID', 'payment.wechat.appId', '', '支付设置'
    UNION ALL SELECT '微信支付商户号', 'payment.wechat.mchId', '', '支付设置'
    UNION ALL SELECT '微信支付APIv3密钥', 'payment.wechat.apiV3Key', '', '支付设置'
    UNION ALL SELECT '微信支付证书', 'payment.wechat.certificate', '', '支付设置'
    UNION ALL SELECT '微信支付私钥', 'payment.wechat.privateKey', '', '支付设置'
    UNION ALL SELECT '支付宝开关', 'payment.alipay.status', '0', '支付设置'
    UNION ALL SELECT '支付宝AppID', 'payment.alipay.appId', '', '支付设置'
    UNION ALL SELECT '支付宝私钥', 'payment.alipay.privateKey', '', '支付设置'
    UNION ALL SELECT '支付宝公钥', 'payment.alipay.publicKey', '', '支付设置'
    UNION ALL SELECT '腾讯地图Key', 'map.tencent.key', '', '地图设置'
    UNION ALL SELECT '天地图Key', 'map.tianditu.key', '', '地图设置'
    UNION ALL SELECT '短信服务商', 'sms.provider', 'aliyun', '短信设置'
    UNION ALL SELECT '阿里云短信状态', 'sms.aliyun.status', '0', '短信设置'
    UNION ALL SELECT '阿里云短信AccessKey', 'sms.aliyun.appKey', '', '短信设置'
    UNION ALL SELECT '阿里云短信SecretKey', 'sms.aliyun.secretKey', '', '短信设置'
    UNION ALL SELECT '阿里云短信签名', 'sms.aliyun.signName', '', '短信设置'
    UNION ALL SELECT '阿里云短信登录模板', 'sms.aliyun.template.login', '', '短信设置'
    UNION ALL SELECT '存储方式', 'upload.storage.provider', 'local', '上传设置'
    UNION ALL SELECT '七牛云空间', 'upload.qiniu.bucket', '', '上传设置'
    UNION ALL SELECT '七牛云域名', 'upload.qiniu.domain', '', '上传设置'
    UNION ALL SELECT '阿里云OSS空间', 'upload.aliyun.bucket', '', '上传设置'
    UNION ALL SELECT '阿里云OSS域名', 'upload.aliyun.domain', '', '上传设置'
    UNION ALL SELECT '腾讯COS空间', 'upload.tencent.bucket', '', '上传设置'
    UNION ALL SELECT '腾讯COS域名', 'upload.tencent.domain', '', '上传设置'
    UNION ALL SELECT '系统通知开关', 'notification.platform.system.enabled', '1', '通知设置'
    UNION ALL SELECT '短信通知开关', 'notification.platform.sms.enabled', '1', '通知设置'
    UNION ALL SELECT '微信通知开关', 'notification.platform.wechat.enabled', '1', '通知设置'
    UNION ALL SELECT '小程序通知开关', 'notification.platform.mini.enabled', '1', '通知设置'
) s
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_config` c WHERE c.`config_key` = s.`config_key`
);
