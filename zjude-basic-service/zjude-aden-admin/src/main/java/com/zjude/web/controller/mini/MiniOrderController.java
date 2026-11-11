package com.zjude.web.controller.mini;

import java.util.List;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.annotation.RepeatSubmit;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.domain.entity.SysUser;
import com.zjude.common.core.domain.model.LoginUser;
import com.zjude.common.core.page.TableDataInfo;
import com.zjude.common.utils.SecurityUtils;
import com.zjude.common.utils.StringUtils;
import com.zjude.system.domain.ZjudgOrder;
import com.zjude.system.domain.ZjudgRefund;
import com.zjude.system.domain.ZjudgServiceReview;
import com.zjude.system.domain.ZjudgOrderExtraTime;
import com.zjude.system.domain.ZjudgService;
import com.zjude.system.service.IZjudgOrderService;
import com.zjude.system.service.IZjudgOrderExtraTimeService;
import com.zjude.system.service.IZjudgRefundService;
import com.zjude.system.service.IZjudgReviewService;

/**
 * 小程序用户订单与评价接口。
 *
 * <p>所有接口都需要登录态：用户身份一律从 token 中解析，不接受客户端传入的 userId，
 * 从而避免通过篡改请求体操作他人订单。</p>
 */
@RestController
@RequestMapping("/api/mini/user")
public class MiniOrderController extends BaseController
{
    @Resource
    private IZjudgOrderService orderService;

    @Resource
    private IZjudgReviewService reviewService;

    @Resource
    private IZjudgRefundService refundService;

    @Resource
    private IZjudgOrderExtraTimeService extraTimeService;

    /**
     * 当前用户的订单列表。
     *
     * @param status 订单状态，为空时查询全部
     * @return 订单分页数据
     */
    @GetMapping("/orders")
    public TableDataInfo myOrders(String status)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        startPage();
        List<ZjudgOrder> rows = orderService.selectListByUser(loginUser.getUserId(), status);
        rows.forEach(item -> item.setMasterPrice(null));
        return getDataTable(rows);
    }

    /**
     * 当前用户订单详情。
     *
     * @param orderId 订单主键
     * @return 订单
     */
    @GetMapping("/orders/{orderId}")
    public AjaxResult orderDetail(@PathVariable String orderId)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        ZjudgOrder order = orderService.selectByIdAndUser(orderId, loginUser.getUserId());
        if (order == null)
        {
            return AjaxResult.error("订单不存在");
        }
        order.setMasterPrice(null);
        AjaxResult result = AjaxResult.success(order);
        // 退款信息单独返回：订单本身只有 payment_status，用户看不到退款到哪一步
        ZjudgRefund refund = refundService.getByOrder(orderId, loginUser.getUserId());
        if (refund != null)
        {
            result.put("refund", refundView(refund));
        }
        if ("1".equals(order.getPaymentStatus()) && ("4".equals(order.getOrderStatus()) || "1".equals(order.getOrderStatus()) || "2".equals(order.getOrderStatus())))
        {
            ZjudgService product = extraTimeService.getProductForOrder(orderId, loginUser.getUserId());
            List<java.util.Map<String, Object>> extraTimes = extraTimeService.listForOrder(orderId, loginUser.getUserId()).stream().map(this::extraTimeView).toList();
            if (product != null)
            {
                java.util.Map<String, Object> productView = new java.util.LinkedHashMap<>();
                productView.put("serviceName", product.getServiceName());
                productView.put("price", product.getPrice());
                productView.put("unitName", product.getUnitName());
                result.put("extraTimeProduct", productView);
            }
            if (!extraTimes.isEmpty()) result.put("extraTimes", extraTimes);
        }
        return result;
    }

    /**
     * 退款单裁剪为小程序可见字段。
     *
     * <p>只保留状态、金额与时间，不返回微信退款单号、失败原始原因等内部字段。</p>
     *
     * @param refund 退款单
     * @return 展示用退款信息
     */
    private java.util.Map<String, Object> refundView(ZjudgRefund refund)
    {
        java.util.Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("refundNo", refund.getRefundNo());
        view.put("refundStatus", refund.getRefundStatus());
        view.put("refundStatusLabel", refundStatusLabel(refund.getRefundStatus()));
        view.put("refundAmount", refund.getRefundAmount());
        view.put("refundTime", refund.getRefundTime());
        view.put("createTime", refund.getCreateTime());
        view.put("remark", refund.getRemark());
        return view;
    }

    /**
     * 退款状态文案，与后台退款记录页保持一致。
     *
     * @param status 状态编码
     * @return 中文文案
     */
    private String refundStatusLabel(String status)
    {
        if (ZjudgRefund.STATUS_PENDING.equals(status))
        {
            return "待审核";
        }
        if (ZjudgRefund.STATUS_PROCESSING.equals(status))
        {
            return "退款中";
        }
        if (ZjudgRefund.STATUS_SUCCESS.equals(status))
        {
            return "退款成功";
        }
        if (ZjudgRefund.STATUS_FAILED.equals(status))
        {
            return "退款失败";
        }
        if (ZjudgRefund.STATUS_REJECTED.equals(status))
        {
            return "退款被拒";
        }
        return "处理中";
    }

    /**
     * 创建订单。
     *
     * @param body 下单参数
     * @return 新建订单
     */
    @RepeatSubmit(interval = 5000, message = "请勿重复提交订单")
    @PostMapping("/orders")
    public AjaxResult createOrder(@Validated @RequestBody CreateOrderBody body)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        ZjudgOrder order = orderService.createOrder(body.serviceId(), body.slotId(), body.appointmentDate(),
                body.quantity(), body.remark(), body.address(), body.phone(), loginUser.getUserId(),
                displayName(loginUser), body.cityId());

        order.setMasterPrice(null);
        return AjaxResult.success("预约成功", order);
    }

    /** 创建关联原订单的补时支付单，补时时长最少一小时。 */
    @RepeatSubmit(interval = 5000, message = "请勿重复提交补时订单")
    @PostMapping("/orders/{orderId}/extra-time")
    public AjaxResult createExtraTime(@PathVariable String orderId, @Validated @RequestBody ExtraTimeBody body)
    {
        ZjudgOrderExtraTime item = extraTimeService.create(orderId, SecurityUtils.getUserId(), body.hours());
        return AjaxResult.success(extraTimeView(item));
    }

    private java.util.Map<String, Object> extraTimeView(ZjudgOrderExtraTime item)
    {
        java.util.Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("extraTimeId", item.getExtraTimeId());
        view.put("extraTimeNo", item.getExtraTimeNo());
        view.put("serviceName", item.getServiceName());
        view.put("hours", item.getHours());
        view.put("amount", item.getAmount());
        view.put("paymentStatus", item.getPaymentStatus());
        view.put("createTime", item.getCreateTime());
        view.put("paidTime", item.getPaidTime());
        return view;
    }

    /**
     * 取消订单。
     *
     * @param orderId 订单主键
     * @return 操作结果
     */
    @PutMapping("/orders/{orderId}/cancel")
    public AjaxResult cancelOrder(@PathVariable String orderId)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        orderService.cancelOrder(orderId, loginUser.getUserId());
        return AjaxResult.success("订单已取消");
    }

    /**
     * 删除当前用户符合条件的已取消订单。
     *
     * @param orderId 订单主键
     * @return 操作结果
     */
    @DeleteMapping("/orders/{orderId}")
    public AjaxResult deleteOrder(@PathVariable String orderId)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        orderService.deleteUserOrder(orderId, loginUser.getUserId());
        return AjaxResult.success("订单已删除");
    }

    /**
     * 确认订单完成。
     *
     * @param orderId 订单主键
     * @return 操作结果
     */
    @PutMapping("/orders/{orderId}/confirm")
    public AjaxResult confirmOrder(@PathVariable String orderId)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        orderService.confirmOrder(orderId, loginUser.getUserId());
        return AjaxResult.success("已确认服务完成");
    }

    /**
     * 提交服务评价。
     *
     * @param body 评价参数
     * @return 新建评价
     */
    @PostMapping("/reviews")
    public AjaxResult submitReview(@Validated @RequestBody ReviewBody body)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        ZjudgServiceReview review = reviewService.submitOrderReview(body.orderId(), body.rating(),
                body.content(), loginUser.getUserId(), displayName(loginUser));
        return AjaxResult.success("评价成功", review);
    }

    /**
     * 生成用于展示的脱敏用户信息，避免把昵称之外的隐私字段写入订单快照。
     *
     * @param loginUser 登录用户
     * @return 展示用名称
     */
    private String displayName(LoginUser loginUser)
    {
        SysUser user = loginUser.getUser();
        if (user == null)
        {
            return null;
        }
        String nickName = StringUtils.isEmpty(user.getNickName()) ? user.getUserName() : user.getNickName();
        if (StringUtils.isEmpty(nickName))
        {
            return null;
        }
        String value = nickName.trim();
        // 昵称脱敏：保留首字符
        if (value.length() > 1)
        {
            return value.charAt(0) + "**";
        }
        return value;
    }

    /** 下单请求体。 */
    public record CreateOrderBody(
            @NotBlank(message = "请选择服务项目") String serviceId,
            @NotBlank(message = "请选择预约时段") String slotId,
            @NotBlank(message = "请选择预约日期") String appointmentDate,
            @NotBlank(message = "请选择服务地址") @Size(max = 255, message = "服务地址不能超过255个字符") String address,
            @NotBlank(message = "请填写联系电话") @Pattern(regexp = "^1\\d{10}$", message = "联系电话格式不正确") String phone,
            @Size(max = 16, message = "城市ID过长") String cityId,
            @Size(max = 500, message = "备注不能超过500个字符") String remark,
            @jakarta.validation.constraints.DecimalMin(value = "0.01", message = "服务数量必须大于0") java.math.BigDecimal quantity)
    {
        public CreateOrderBody(String serviceId, String slotId, String appointmentDate, String address, String phone,
                String cityId, String remark)
        {
            this(serviceId, slotId, appointmentDate, address, phone, cityId, remark, null);
        }
    }

    /** 评价请求体。 */
    public record ReviewBody(
            @NotBlank(message = "订单不能为空") String orderId,
            @Min(value = 1, message = "评分最低为1") @Max(value = 5, message = "评分最高为5") Integer rating,
            @NotBlank(message = "请填写评价内容") @Size(max = 1000, message = "评价内容不能超过1000个字符") String content)
    {
    }

    public record ExtraTimeBody(
            @NotNull @jakarta.validation.constraints.Min(value = 1, message = "补时时长至少1小时")
            @jakarta.validation.constraints.Max(value = 24, message = "补时时长最多24小时") Integer hours)
    {
    }
}
