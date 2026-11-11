package com.zjude.web.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.zjude.common.core.domain.entity.SysUser;
import com.zjude.common.utils.StringUtils;
import com.zjude.integrated.wechat.WechatConfigKeys;
import com.zjude.integrated.wechat.WechatConfigService;
import com.zjude.integrated.wechat.WechatSubscribeMessageService;
import com.zjude.system.domain.ZjudgMaster;
import com.zjude.system.domain.ZjudgOrder;
import com.zjude.system.mapper.SysUserMapper;
import com.zjude.system.mapper.ZjudgMasterMapper;
import com.zjude.system.service.IZjudgNotifyService;

/**
 * 家政订单小程序订阅消息实现。
 *
 * <p>放在 admin 模块是因为 {@code admin → integrated → framework → system} 的依赖方向：
 * 微信能力属于 integrated，而业务服务在 system，反向依赖会形成循环。</p>
 *
 * <p>所有方法都是「尽力而为」：推送失败只记录日志，绝不影响下单、接单、完工等业务事务。
 * 模板 ID 未配置、订阅开关关闭、用户未授权时静默跳过。</p>
 */
@Service("zjudgNotifyService")
public class ZjudgNotifyServiceImpl implements IZjudgNotifyService
{
    private static final Logger log = LoggerFactory.getLogger(ZjudgNotifyServiceImpl.class);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String[] DEFAULT_ACCEPTED = { "thing1", "time2", "character_string3", "thing4" };

    private static final String[] DEFAULT_PAID = { "thing1", "character_string2", "amount3", "thing4" };

    private static final String[] DEFAULT_NEW_ORDER = { "thing1", "time2", "thing3", "character_string4" };

    private static final String[] DEFAULT_ADMIN_ORDER = { "thing1", "time2", "character_string3", "thing4" };

    @Resource
    private WechatSubscribeMessageService subscribeMessageService;

    @Resource
    private WechatConfigService wechatConfigService;

    @Resource
    private SysUserMapper userMapper;

    @Resource
    private ZjudgMasterMapper masterMapper;

    @Override
    public void notifyOrderCreatedToAdmins(ZjudgOrder order)
    {
        notifyAdmins(order, WechatConfigKeys.TEMPLATE_ADMIN_CREATED, WechatConfigKeys.FIELD_ADMIN_CREATED,
                "新订单已提交，当前待支付");
    }

    @Override
    public void notifyAcceptedToAdmins(ZjudgOrder order)
    {
        notifyAdmins(order, WechatConfigKeys.TEMPLATE_ADMIN_ACCEPTED, WechatConfigKeys.FIELD_ADMIN_ACCEPTED,
                "师傅已接单");
    }

    @Override
    public void notifyAccepted(ZjudgOrder order)
    {
        if (order == null)
        {
            return;
        }
        // 默认字段与「师傅已接单」模板对应；运营换模板后可在这里配置字段名
        Map<String, String> fields = fields(WechatConfigKeys.FIELD_ACCEPTED,
                new String[] { "thing1", "time2", "character_string3", "thing4" });
        Map<String, String> data = new LinkedHashMap<>();
        data.put(fields.get("service"), truncate(order.getServiceName(), 20));
        data.put(fields.get("time"), formatAppointment(order));
        data.put(fields.get("orderNo"), order.getOrderNo());
        data.put(fields.get("remark"), "师傅已接单，请保持电话畅通");
        sendToUser(order.getUserId(), WechatConfigKeys.TEMPLATE_ACCEPTED, pageOrder(order), data);
    }

    private void notifyAdmins(ZjudgOrder order, String templateKey, String fieldKey, String remark)
    {
        if (order == null)
        {
            return;
        }
        Map<String, String> fields = fields(fieldKey, DEFAULT_ADMIN_ORDER);
        Map<String, String> data = new LinkedHashMap<>();
        data.put(fields.get("service"), truncate(order.getServiceName(), 20));
        data.put(fields.get("time"), formatAppointment(order));
        data.put(fields.get("orderNo"), order.getOrderNo());
        data.put(fields.get("remark"), remark);

        try
        {
            List<SysUser> admins = userMapper.selectOrderNotificationAdmins();
            if (admins == null || admins.isEmpty())
            {
                log.debug("管理员订阅消息跳过：没有绑定小程序的启用管理员账号 templateKey={}", templateKey);
                return;
            }
            for (SysUser admin : admins)
            {
                if (admin != null)
                {
                    sendToUser(admin.getUserId(), templateKey, pageAdminOrder(), data);
                }
            }
        }
        catch (Exception exception)
        {
            // 管理员消息也只是尽力发送，不得回滚订单事务
            log.warn("管理员订阅消息发送异常 key={} orderId={}", templateKey, order.getOrderId(), exception);
        }
    }

    @Override
    public void notifyFinished(ZjudgOrder order)
    {
        if (order == null)
        {
            return;
        }
        Map<String, String> fields = fields(WechatConfigKeys.FIELD_FINISHED,
                new String[] { "thing1", "time2", "character_string3", "thing4" });
        Map<String, String> data = new LinkedHashMap<>();
        data.put(fields.get("service"), truncate(order.getServiceName(), 20));
        data.put(fields.get("time"), formatAppointment(order));
        data.put(fields.get("orderNo"), order.getOrderNo());
        data.put(fields.get("remark"), "服务已完成，请查看订单详情并评价");
        sendToUser(order.getUserId(), WechatConfigKeys.TEMPLATE_FINISHED, pageOrder(order), data);
    }

    @Override
    public void notifyPaid(ZjudgOrder order)
    {
        if (order == null)
        {
            return;
        }
        Map<String, String> fields = fields(WechatConfigKeys.FIELD_PAID,
                new String[] { "thing1", "character_string2", "amount3", "thing4" });
        Map<String, String> data = new LinkedHashMap<>();
        data.put(fields.get("service"), truncate(order.getServiceName(), 20));
        data.put(fields.get("orderNo"), order.getOrderNo());
        data.put(fields.get("amount"), order.getAmount() == null ? "0.00" : order.getAmount().toPlainString() + "元");
        data.put(fields.get("remark"), "支付成功，已为您安排服务");
        sendToUser(order.getUserId(), WechatConfigKeys.TEMPLATE_PAID, pageOrder(order), data);
    }

    @Override
    public void notifyNewOrderToMaster(String masterId, ZjudgOrder order)
    {
        if (StringUtils.isEmpty(masterId) || order == null)
        {
            return;
        }
        ZjudgMaster master = masterMapper.selectById(masterId);
        if (master == null || StringUtils.isEmpty(master.getUserId()))
        {
            return;
        }
        Map<String, String> fields = fields(WechatConfigKeys.FIELD_NEW_ORDER,
                new String[] { "thing1", "time2", "thing3", "character_string4" });
        Map<String, String> data = new LinkedHashMap<>();
        data.put(fields.get("service"), truncate(order.getServiceName(), 20));
        data.put(fields.get("time"), formatAppointment(order));
        data.put(fields.get("address"), truncate(order.getAddress(), 20));
        data.put(fields.get("orderNo"), order.getOrderNo());
        sendToUser(master.getUserId(), WechatConfigKeys.TEMPLATE_NEW_ORDER, pageMasterOrder(), data);
    }

    /**
     * 解析模板字段名配置。
     *
     * <p>配置格式为逗号分隔的四个字段名，顺序固定为「服务 / 时间 / 订单号 / 备注」，
     * 未配置或格式不完整时回落到内置默认值，保证换模板前老配置照常工作。</p>
     *
     * @param configKey 配置键
     * @param defaults 默认字段名，顺序为 service / time / 附加 / remark
     * @return 语义字段名映射
     */
    private Map<String, String> fields(String configKey, String[] defaults)
    {
        String[] names = defaults;
        String raw = wechatConfigService.get(configKey);
        if (StringUtils.isNotEmpty(raw))
        {
            String[] parsed = raw.split(",");
            if (parsed.length == defaults.length)
            {
                names = new String[parsed.length];
                for (int index = 0; index < parsed.length; index++)
                {
                    names[index] = parsed[index].trim();
                }
            }
            else
            {
                log.warn("订阅消息字段配置格式不正确，使用默认值 key={} value={}", configKey, raw);
            }
        }

        // 第三个字段在不同模板里语义不同：接单/完工是订单号，支付是金额，新单是地址
        String third;
        if (defaults == DEFAULT_PAID)
        {
            third = "amount";
        }
        else if (defaults == DEFAULT_NEW_ORDER)
        {
            third = "address";
        }
        else
        {
            third = "orderNo";
        }

        Map<String, String> result = new LinkedHashMap<>();
        result.put("service", names[0]);
        result.put("time", names[1]);
        result.put(third, names[2]);
        result.put("remark", names[3]);
        return result;
    }

    /**
     * 按用户主键查 openid 后发送。所有异常在此吞掉，保证不影响调用方事务。
     *
     * @param userId 用户主键
     * @param templateKey 模板配置键
     * @param page 跳转页
     * @param data 模板数据
     */
    private void sendToUser(String userId, String templateKey, String page, Map<String, String> data)
    {
        try
        {
            if (StringUtils.isEmpty(userId))
            {
                return;
            }
            var user = userMapper.selectUserById(userId);
            if (user == null || StringUtils.isEmpty(user.getWxOpenid()))
            {
                log.debug("订阅消息跳过：用户未绑定 openid userId={}", userId);
                return;
            }
            String templateId = wechatConfigService.template(templateKey);
            if (StringUtils.isEmpty(templateId))
            {
                log.debug("订阅消息跳过：模板未配置 key={}", templateKey);
                return;
            }
            subscribeMessageService.send(user.getWxOpenid(), templateId, page, data);
        }
        catch (Exception exception)
        {
            // 通知失败绝不向上抛，避免阻断主流程
            log.warn("订阅消息发送异常 key={} userId={}", templateKey, userId, exception);
        }
    }

    private String formatAppointment(ZjudgOrder order)
    {
        if (order.getAppointmentDate() == null)
        {
            return "";
        }
        // java.sql.Date 不支持 toInstant()；下单和 MyBatis 日期列通常会返回该类型。
        LocalDate appointment = order.getAppointmentDate() instanceof java.sql.Date sqlDate
                ? sqlDate.toLocalDate()
                : order.getAppointmentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String date = appointment.format(DATE_FORMAT);
        return StringUtils.isEmpty(order.getSlotName()) ? date : date + " " + order.getSlotName();
    }

    private String truncate(String value, int max)
    {
        if (value == null)
        {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }

    private String pageOrder(ZjudgOrder order)
    {
        String page = wechatConfigService.get(WechatConfigKeys.PAGE_ORDER);
        page = StringUtils.isEmpty(page) ? "/pages/order-detail/index" : page;
        // 兼容旧配置：旧默认页是订单列表，通知应直接打开本次订单详情。
        if ("/pages/orders/index".equals(page))
        {
            page = "/pages/order-detail/index";
        }
        if (order == null || StringUtils.isEmpty(order.getOrderId()) || page.contains("id="))
        {
            return page;
        }
        return page + (page.contains("?") ? "&" : "?") + "id=" + order.getOrderId();
    }

    private String pageMasterOrder()
    {
        String page = wechatConfigService.get(WechatConfigKeys.PAGE_MASTER_ORDER);
        return StringUtils.isEmpty(page) ? "/pages/master/orders/index" : page;
    }

    private String pageAdminOrder()
    {
        String page = wechatConfigService.get(WechatConfigKeys.PAGE_ADMIN_ORDER);
        return StringUtils.isEmpty(page) ? "/pages/home/index" : page;
    }
}
