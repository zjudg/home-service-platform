package com.zjude.integrated.wechat;

/**
 * 微信小程序相关配置在 sys_config 中使用的键名。
 *
 * <p>配置由后台「微信设置」页面维护，服务端不依赖 application.yml，
 * 便于运营在拿到 AppID 后无需重新发布即可生效。</p>
 */
public final class WechatConfigKeys
{
    /** 小程序登录总开关（1 开启 / 0 关闭） */
    public static final String LOGIN_ENABLED = "app.wechatLogin";

    /** 小程序 AppID */
    public static final String APP_ID = "app.wechat.appId";

    /** 小程序 AppSecret */
    public static final String APP_SECRET = "app.wechat.appSecret";

    /** 订阅消息总开关（1 开启 / 0 关闭） */
    public static final String SUBSCRIBE_ENABLED = "notification.mini.enabled";

    /** 订阅消息模板：师傅接单通知用户 */
    public static final String TEMPLATE_ACCEPTED = "notification.mini.template.accepted";

    /** 订阅消息模板：服务完工通知用户 */
    public static final String TEMPLATE_FINISHED = "notification.mini.template.finished";

    /** 订阅消息模板：支付成功通知用户 */
    public static final String TEMPLATE_PAID = "notification.mini.template.paid";

    /** 订阅消息模板：新订单提醒师傅 */
    public static final String TEMPLATE_NEW_ORDER = "notification.mini.template.newOrder";

    /** 订阅消息模板：新订单提醒管理员 */
    public static final String TEMPLATE_ADMIN_CREATED = "notification.mini.template.adminCreated";

    /** 订阅消息模板：师傅接单提醒管理员 */
    public static final String TEMPLATE_ADMIN_ACCEPTED = "notification.mini.template.adminAccepted";

    /** 用户侧通知跳转页 */
    public static final String PAGE_ORDER = "notification.mini.page.order";

    /** 师傅侧通知跳转页 */
    public static final String PAGE_MASTER_ORDER = "notification.mini.page.masterOrder";

    /** 管理员侧订单通知跳转页 */
    public static final String PAGE_ADMIN_ORDER = "notification.mini.page.adminOrder";

    /** 师傅接单通知：模板字段名配置，留空时使用代码内置默认值 */
    public static final String FIELD_ACCEPTED = "notification.mini.field.accepted";

    /** 服务完工通知：模板字段名配置 */
    public static final String FIELD_FINISHED = "notification.mini.field.finished";

    /** 支付成功通知：模板字段名配置 */
    public static final String FIELD_PAID = "notification.mini.field.paid";

    /** 师傅新单提醒：模板字段名配置 */
    public static final String FIELD_NEW_ORDER = "notification.mini.field.newOrder";

    /** 管理员新订单提醒：模板字段名配置 */
    public static final String FIELD_ADMIN_CREATED = "notification.mini.field.adminCreated";

    /** 师傅接单管理员提醒：模板字段名配置 */
    public static final String FIELD_ADMIN_ACCEPTED = "notification.mini.field.adminAccepted";

    private WechatConfigKeys()
    {
    }
}
