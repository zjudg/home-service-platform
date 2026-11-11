package com.zjude.system.service.impl;

import java.text.SimpleDateFormat;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zjude.common.annotation.DataScope;
import com.zjude.common.enums.ZjudgOrderStatus;
import com.zjude.common.enums.ZjudgOrderPaymentSource;
import com.zjude.common.enums.ZjudgUnitMeasureType;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.ZjudgBookingSlot;
import com.zjude.system.domain.ZjudgMaster;
import com.zjude.system.domain.ZjudgOrder;
import com.zjude.system.domain.ZjudgService;
import com.zjude.system.mapper.ZjudgBookingSlotMapper;
import com.zjude.system.mapper.ZjudgMasterMapper;
import com.zjude.system.mapper.ZjudgOrderMapper;
import com.zjude.system.mapper.ZjudgServiceMapper;
import com.zjude.system.service.IZjudgNotifyService;
import com.zjude.system.service.IZjudgCityScopeService;
import com.zjude.system.service.IZjudgSettleService;
import com.zjude.system.service.IZjudgOrderService;

/**
 * 家政服务订单服务实现。
 *
 * <p>下单时不信任客户端传入的金额与名称：价格取服务主数据，名称取服务与服务单位快照，
 * 预约容量以 zjudg_booking_slot.max_reserve 为准。</p>
 */
@Service("zjudgOrderService")
public class ZjudgOrderServiceImpl implements IZjudgOrderService
{
    /** 订单状态编码：待确认 */
    private static final String STATUS_PENDING = ZjudgOrderStatus.PENDING.getCode();

    /** 支付状态编码：未支付 */
    private static final String PAYMENT_UNPAID = "0";
    /** 支付状态编码：已支付。 */
    private static final String PAYMENT_PAID = "1";

    /** 订单号日期部分格式 */
    private static final String ORDER_NO_DATE_PATTERN = "yyyyMMdd";

    /** 预约日期格式 */
    private static final DateTimeFormatter APPOINTMENT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private ZjudgOrderMapper mapper;

    @Resource
    private ZjudgServiceMapper serviceMapper;

    @Resource
    private ZjudgBookingSlotMapper slotMapper;

    @Resource
    private ZjudgMasterMapper masterMapper;

    @Resource
    private IZjudgNotifyService notifyService;

    @Resource
    private IZjudgSettleService settleService;

    @Resource
    private IZjudgCityScopeService cityScopeService;

    @Resource
    private BaseKeyGenerate keyGenerate;

    @Override
    @DataScope(userAlias = "o")
    public List<ZjudgOrder> selectList(ZjudgOrder query)
    {
        return mapper.selectList(query);
    }

    @Override
    @DataScope(userAlias = "o", createByAlias = "o")
    public List<ZjudgOrder> selectAdminList(ZjudgOrder query)
    {
        return mapper.selectAdminList(query);
    }

    @Override
    public ZjudgOrder selectAdminById(String id)
    {
        return mapper.selectAdminById(id);
    }

    @Override
    public List<ZjudgOrder> selectListByUser(String userId, String orderStatus)
    {
        return mapper.selectListByUser(userId, orderStatus);
    }

    @Override
    public ZjudgOrder selectById(String id)
    {
        return mapper.selectById(id);
    }

    @Override
    public ZjudgOrder selectByIdAndUser(String orderId, String userId)
    {
        if (StringUtils.isEmpty(orderId) || StringUtils.isEmpty(userId))
        {
            return null;
        }
        return mapper.selectByIdAndUser(orderId, userId);
    }

    @Override
    public int countActiveBySlot(String slotId, String appointmentDate)
    {
        return mapper.countActiveBySlot(slotId, appointmentDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZjudgOrder createOrder(String serviceId, String slotId, String appointmentDate, BigDecimal quantity,
            String remark, String address, String phone, String userId, String userInfo,
            String cityId)
    {
        if (StringUtils.isEmpty(userId))
        {
            throw new ServiceException("请先登录后再预约服务");
        }
        return createOrderInternal(serviceId, slotId, appointmentDate, quantity, remark, address, phone, userId,
                userInfo, cityId, PAYMENT_UNPAID, ZjudgOrderPaymentSource.WECHAT, userId, true, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZjudgOrder createOfflinePaidOrder(String serviceId, String slotId, String appointmentDate,
            BigDecimal quantity, String remark, String address, String phone, String customerName, String cityId,
            String operator)
    {
        String safeName = StringUtils.isEmpty(customerName) ? "" : customerName.trim();
        String safePhone = StringUtils.isEmpty(phone) ? "" : phone.trim();
        String safeAddress = StringUtils.isEmpty(address) ? "" : address.trim();
        if (safeName.isEmpty() || !safePhone.matches("^1\\d{10}$") || safeAddress.isEmpty())
        {
            throw new ServiceException("请填写有效的线下客户姓名、联系电话和服务地址");
        }
        if (StringUtils.isEmpty(operator))
        {
            throw new ServiceException("无法识别当前管理员");
        }
        return createOrderInternal(serviceId, slotId, appointmentDate, quantity, remark, address, phone, null,
                safeName, cityId, PAYMENT_PAID, ZjudgOrderPaymentSource.OFFLINE, operator, false, true);
    }

    private ZjudgOrder createOrderInternal(String serviceId, String slotId, String appointmentDate, BigDecimal quantity,
            String remark, String address, String phone, String userId, String userInfo, String cityId,
            String paymentStatus, ZjudgOrderPaymentSource paymentSource, String createBy, boolean notifyAdmins,
            boolean validateCityScope)
    {
        ZjudgService service = requireBookableService(serviceId);
        ZjudgBookingSlot slot = requireBookableSlot(slotId);
        if (validateCityScope)
        {
            validateOfflineCityScope(service, slot, cityId);
        }
        ZjudgUnitMeasureType measureType = ZjudgUnitMeasureType.fromCode(service.getMeasureType());
        if (measureType == null)
        {
            throw new ServiceException("服务单位计量方式未配置");
        }
        if (measureType == ZjudgUnitMeasureType.HOUR
                && (quantity == null || quantity.stripTrailingZeros().scale() > 0
                        || quantity.compareTo(BigDecimal.valueOf(2)) < 0))
        {
            throw new ServiceException("预约时长必须是整数且不少于2小时");
        }
        if (measureType == ZjudgUnitMeasureType.AREA
                && (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0
                        || quantity.stripTrailingZeros().scale() > 2))
        {
            throw new ServiceException("请填写有效的服务面积");
        }
        if (measureType == ZjudgUnitMeasureType.COUNT
                && (quantity == null || quantity.stripTrailingZeros().scale() > 0
                        || quantity.compareTo(BigDecimal.ONE) < 0))
        {
            throw new ServiceException("请填写有效的服务数量");
        }
        if (measureType == ZjudgUnitMeasureType.NONE) quantity = null;
        LocalDate date = parseAppointmentDate(appointmentDate);
        assertCapacityAvailable(slot, date);

        ZjudgOrder order = new ZjudgOrder();
        order.setOrderId(keyGenerate.snowflakeStringKey("ZO_"));
        order.setOrderNo(nextOrderNo());
        order.setServiceId(service.getServiceId());
        order.setServiceName(service.getServiceName());
        // 城市归属：优先取服务发布城市，服务为全国时取下单时选择的城市
        order.setCityId(StringUtils.isNotEmpty(service.getCityId()) ? service.getCityId() : cityId);
        order.setUserId(userId);
        order.setUserInfo(truncate(userInfo, 128));
        order.setAppointmentDate(java.sql.Date.valueOf(date));
        order.setSlotId(slot.getSlotId());
        order.setSlotName(slot.getSlotName());
        order.setQuantity(quantity);
        order.setQuantityUnit(quantity == null ? null : service.getUnitName());
        order.setAddress(truncate(address, 255));
        order.setPhone(truncate(phone, 20));
        // 金额只取服务端主数据，忽略客户端提交的价格
        order.setAmount((quantity == null ? service.getPrice() : service.getPrice().multiply(quantity))
                .setScale(2, RoundingMode.HALF_UP));
        BigDecimal payoutQuantity = quantity == null ? BigDecimal.ONE : quantity;
        order.setMasterPrice(service.getMasterPrice().multiply(payoutQuantity).setScale(2, RoundingMode.HALF_UP));
        order.setOrderStatus(STATUS_PENDING);
        order.setPaymentStatus(paymentStatus);
        order.setPaymentSource(paymentSource.getCode());
        order.setRemark(truncate(remark, 500));
        order.setCreateBy(createBy);
        mapper.insert(order);

        ZjudgOrder saved = mapper.selectById(order.getOrderId());
        ZjudgOrder result = saved == null ? order : saved;
        // 小程序下单提醒管理员继续跟进支付；线下单由管理员录入，不再重复通知管理员。
        if (notifyAdmins)
        {
            notifyService.notifyOrderCreatedToAdmins(result);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelOrder(String orderId, String userId)
    {
        return transitionOwned(orderId, userId, ZjudgOrderStatus.CANCELLED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmOrder(String orderId, String userId)
    {
        return transitionOwned(orderId, userId, ZjudgOrderStatus.FINISHED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStatus(String orderId, String target, String operator)
    {
        ZjudgOrder order = mapper.selectById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        ZjudgOrderStatus current = ZjudgOrderStatus.of(order.getOrderStatus());
        ZjudgOrderStatus next = ZjudgOrderStatus.of(target);
        if (next == null)
        {
            throw new ServiceException("未知的订单状态");
        }
        if (current != null && !current.canTransitionTo(next))
        {
            throw new ServiceException("订单当前状态不允许变更为" + next.getLabel());
        }
        return mapper.updateStatus(orderId, next.getCode(), operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataScope(userAlias = "o", createByAlias = "o")
    public int deleteAdminOrder(ZjudgOrder query)
    {
        if (query == null || StringUtils.isEmpty(query.getOrderId()))
        {
            throw new ServiceException("订单编号不能为空");
        }
        List<ZjudgOrder> visibleOrders = mapper.selectAdminList(query);
        if (visibleOrders.isEmpty())
        {
            throw new ServiceException("订单不存在");
        }
        ZjudgOrder order = visibleOrders.get(0);
        if (!ZjudgOrderStatus.CANCELLED.getCode().equals(order.getOrderStatus())
                || !PAYMENT_UNPAID.equals(order.getPaymentStatus())
                || StringUtils.isNotEmpty(order.getMasterId()))
        {
            throw new ServiceException("仅已取消、未支付且未指派的订单可以删除");
        }
        int rows = mapper.deleteAdminIfUnreferenced(query.getOrderId());
        if (rows == 0)
        {
            throw new ServiceException("订单存在支付、退款、评价或履约记录，无法删除；请保留订单以维护业务记录");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserOrder(String orderId, String userId)
    {
        if (StringUtils.isEmpty(orderId) || StringUtils.isEmpty(userId))
        {
            throw new ServiceException("订单不存在");
        }
        ZjudgOrder order = mapper.selectByIdAndUser(orderId, userId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (!ZjudgOrderStatus.CANCELLED.getCode().equals(order.getOrderStatus())
                || !PAYMENT_UNPAID.equals(order.getPaymentStatus())
                || StringUtils.isNotEmpty(order.getMasterId()))
        {
            throw new ServiceException("仅已取消、未支付且未指派的订单可以删除");
        }
        int rows = mapper.deleteUserIfUnreferenced(orderId, userId);
        if (rows == 0)
        {
            throw new ServiceException("订单存在支付、退款、评价或履约记录，无法删除");
        }
        return rows;
    }

    @Override
    public ZjudgOrder selectReviewableOrder(String orderId, String userId)
    {
        if (StringUtils.isEmpty(orderId) || StringUtils.isEmpty(userId))
        {
            return null;
        }
        return mapper.selectFinishedByUser(orderId, userId);
    }

    /**
     * 校验订单归属并按状态机流转，供取消与确认复用。
     *
     * @param orderId 订单主键
     * @param userId 用户主键
     * @param target 目标状态
     * @return 影响行数
     */
    private int transitionOwned(String orderId, String userId, ZjudgOrderStatus target)
    {
        if (StringUtils.isEmpty(userId))
        {
            throw new ServiceException("请先登录");
        }
        ZjudgOrder order = mapper.selectByIdAndUser(orderId, userId);
        if (order == null)
        {
            // 不区分"不存在"与"不属于本人"，避免泄露他人订单是否存在
            throw new ServiceException("订单不存在");
        }
        ZjudgOrderStatus current = ZjudgOrderStatus.of(order.getOrderStatus());
        if (current == null)
        {
            throw new ServiceException("订单状态异常，请联系客服");
        }
        if (current.isFinal())
        {
            throw new ServiceException("订单已完成或已取消，无法再操作");
        }
        if (!current.canTransitionTo(target))
        {
            throw new ServiceException("订单当前状态不允许变更为" + target.getLabel());
        }
        int rows = mapper.updateStatusOwned(orderId, userId, current.getCode(), target.getCode(), userId);
        if (rows == 0)
        {
            // 并发下单/重复提交导致状态已被他人改变
            throw new ServiceException("订单状态已变更，请刷新后重试");
        }
        if (target == ZjudgOrderStatus.FINISHED)
        {
            // 用户确认完成同样是完单口径，需同步师傅完单数与评分，并触发结算
            refreshMasterStatistics(order.getMasterId());
            settleService.settleOrder(orderId);
        }
        return rows;
    }

    @Override
    public List<ZjudgOrder> selectPendingPool(String slotId)
    {
        return mapper.selectPendingPool(LocalDate.now().format(APPOINTMENT_FORMAT), slotId);
    }

    @Override
    public List<ZjudgOrder> selectPendingPool(String slotId, String masterId, boolean onlyMatched)
    {
        String today = LocalDate.now().format(APPOINTMENT_FORMAT);
        if (!onlyMatched || StringUtils.isEmpty(masterId))
        {
            return mapper.selectPendingPool(today, slotId);
        }
        ZjudgMaster master = masterMapper.selectById(masterId);
        List<String> skills = parseSkills(master == null ? null : master.getSkillCategoryIds());
        if (skills.isEmpty())
        {
            // 师傅未配置技能时回退为全部，避免因配置缺失导致看不到任何订单
            return mapper.selectPendingPool(today, slotId);
        }
        return mapper.selectPendingPoolBySkills(today, slotId, skills);
    }

    /**
     * 解析逗号分隔的技能分类。
     *
     * @param raw 原始字符串
     * @return 去空去重的分类列表
     */
    private List<String> parseSkills(String raw)
    {
        if (StringUtils.isEmpty(raw))
        {
            return List.of();
        }
        return java.util.Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .distinct()
                .toList();
    }

    @Override
    public List<ZjudgOrder> selectByMaster(String masterId, String orderStatus)
    {
        if (StringUtils.isEmpty(masterId))
        {
            return List.of();
        }
        ZjudgOrder query = new ZjudgOrder();
        query.setMasterId(masterId);
        if (!StringUtils.isEmpty(orderStatus))
        {
            query.setOrderStatus(orderStatus);
        }
        return mapper.selectList(query);
    }

    @Override
    public ZjudgOrder selectForMaster(String orderId, String masterId)
    {
        if (StringUtils.isEmpty(orderId) || StringUtils.isEmpty(masterId))
        {
            return null;
        }
        return mapper.selectByIdAndMaster(orderId, masterId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int claimOrder(String orderId, String masterId)
    {
        if (StringUtils.isEmpty(masterId))
        {
            throw new ServiceException("请先完成师傅入驻并通过审核");
        }
        ZjudgOrder order = mapper.selectById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (StringUtils.isNotEmpty(order.getMasterId()))
        {
            throw new ServiceException("订单已被其他师傅接单");
        }
        ZjudgOrderStatus current = ZjudgOrderStatus.of(order.getOrderStatus());
        if (current != ZjudgOrderStatus.PENDING)
        {
            throw new ServiceException("订单当前状态不可接单");
        }
        if (!PAYMENT_PAID.equals(order.getPaymentStatus()))
        {
            throw new ServiceException("订单尚未完成收款，暂不可接单");
        }
        // 禁止师傅承接自己下的订单，否则可自接自完刷单与评分
        ZjudgMaster master = masterMapper.selectById(masterId);
        if (master != null && StringUtils.isNotEmpty(master.getUserId())
                && master.getUserId().equals(order.getUserId()))
        {
            throw new ServiceException("不能承接自己提交的订单");
        }
        // 并发保护：where master_id is null，返回 0 说明被其他师傅抢先
        int rows = mapper.claimOrder(orderId, masterId, current.getCode(),
                ZjudgOrderStatus.ACCEPTED.getCode(), masterId);
        if (rows == 0)
        {
            throw new ServiceException("订单已被其他师傅接单");
        }
        // 通知失败不影响接单结果，服务内部已吞掉异常
        ZjudgOrder acceptedOrder = mapper.selectById(orderId);
        notifyService.notifyAccepted(acceptedOrder);
        notifyService.notifyAcceptedToAdmins(acceptedOrder);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int startService(String orderId, String masterId)
    {
        return transitionByMaster(orderId, masterId, ZjudgOrderStatus.SERVING, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int finishService(String orderId, String masterId, String finishPhotos)
    {
        String photos = truncate(finishPhotos, 1000);
        int rows = transitionByMaster(orderId, masterId, ZjudgOrderStatus.FINISHED, photos);
        refreshMasterStatistics(masterId);
        // 完工即结算：师傅收入在订单完成时入账，退款时再冲回
        settleService.settleOrder(orderId);
        // 通知用户确认与评价，失败不影响完工
        notifyService.notifyFinished(mapper.selectById(orderId));
        return rows;
    }

    /**
     * 师傅侧状态流转：校验归属、终态与状态机规则，并用期望状态做并发保护。
     *
     * @param orderId 订单主键
     * @param masterId 师傅主键
     * @param target 目标状态
     * @param finishPhotos 完工凭证，仅完工时有值
     * @return 影响行数
     */
    private int transitionByMaster(String orderId, String masterId, ZjudgOrderStatus target, String finishPhotos)
    {
        if (StringUtils.isEmpty(masterId))
        {
            throw new ServiceException("请先完成师傅入驻并通过审核");
        }
        ZjudgOrder order = mapper.selectByIdAndMaster(orderId, masterId);
        if (order == null)
        {
            // 不区分「不存在」与「不归属本师傅」，避免泄露他人订单是否存在
            throw new ServiceException("订单不存在");
        }
        ZjudgOrderStatus current = ZjudgOrderStatus.of(order.getOrderStatus());
        if (current == null)
        {
            throw new ServiceException("订单状态异常，请联系客服");
        }
        if (current.isFinal())
        {
            throw new ServiceException("订单已完成或已取消，无法再操作");
        }
        if (!current.canTransitionTo(target))
        {
            throw new ServiceException("订单当前状态不允许变更为" + target.getLabel());
        }
        int rows;
        if (target == ZjudgOrderStatus.FINISHED)
        {
            rows = mapper.finishService(orderId, masterId, current.getCode(), target.getCode(), finishPhotos, masterId);
        }
        else if (target == ZjudgOrderStatus.SERVING)
        {
            rows = mapper.startService(orderId, masterId, current.getCode(), target.getCode(), masterId);
        }
        else
        {
            rows = mapper.updateStatusByMaster(orderId, masterId, current.getCode(), target.getCode(), masterId);
        }
        if (rows == 0)
        {
            throw new ServiceException("订单状态已变更，请刷新后重试");
        }
        return rows;
    }

    /**
     * 完工后回写师傅评分与完单数。统计失败不影响完工事务的语义，但仍在同一事务内重算。
     *
     * @param masterId 师傅主键
     */
    @Override
    public void refreshMasterStatistics(String masterId)
    {
        if (StringUtils.isEmpty(masterId))
        {
            return;
        }
        java.math.BigDecimal rating = mapper.selectMasterRating(masterId);
        int orderCount = mapper.countFinishedByMaster(masterId);
        masterMapper.updateStatistics(masterId, rating == null ? java.math.BigDecimal.ZERO : rating, orderCount);
    }

    /**
     * 校验服务存在、上架且配置了价格。
     *
     * @param serviceId 服务主键
     * @return 服务
     */
    private ZjudgService requireBookableService(String serviceId)
    {
        if (StringUtils.isEmpty(serviceId))
        {
            throw new ServiceException("请选择服务项目");
        }
        ZjudgService service = serviceMapper.selectById(serviceId);
        if (service == null || !"0".equals(service.getStatus()))
        {
            throw new ServiceException("服务不存在或已下架");
        }
        if (Integer.valueOf(1).equals(service.getExtraTimeOnly()))
        {
            throw new ServiceException("补时专用商品只能从原订单详情中购买");
        }
        if (service.getPrice() == null || service.getPrice().signum() < 0)
        {
            throw new ServiceException("服务价格异常，暂不可下单");
        }
        if (service.getMasterPrice() == null || service.getMasterPrice().signum() < 0
                || service.getMasterPrice().compareTo(service.getPrice()) > 0)
        {
            throw new ServiceException("服务接单价配置异常，暂不可下单");
        }
        return service;
    }

    /**
     * 校验预约时段存在且启用。
     *
     * @param slotId 时段主键
     * @return 时段
     */
    private ZjudgBookingSlot requireBookableSlot(String slotId)
    {
        if (StringUtils.isEmpty(slotId))
        {
            throw new ServiceException("请选择预约时段");
        }
        // 加行锁读取：同一时段的下单请求在此串行化，避免并发下容量校验通过后超卖
        ZjudgBookingSlot slot = slotMapper.selectByIdForUpdate(slotId);
        if (slot == null || !"0".equals(slot.getStatus()))
        {
            throw new ServiceException("预约时段不可用");
        }
        return slot;
    }

    /** 服务和预约时段必须都属于管理端选择的服务城市。 */
    private void validateOfflineCityScope(ZjudgService service, ZjudgBookingSlot slot, String cityId)
    {
        if (StringUtils.isEmpty(cityId))
        {
            throw new ServiceException("请选择服务城市");
        }
        if (StringUtils.isNotEmpty(service.getCityId()) && !service.getCityId().equals(cityId))
        {
            throw new ServiceException("所选服务不适用于当前城市");
        }
        if (StringUtils.isNotEmpty(slot.getCityId()) && !slot.getCityId().equals(cityId))
        {
            throw new ServiceException("所选预约时段不适用于当前城市");
        }
        if (cityScopeService.strictFilterEnabled()
                && (StringUtils.isEmpty(service.getCityId()) || StringUtils.isEmpty(slot.getCityId())))
        {
            throw new ServiceException("当前城市仅允许使用城市专属服务和时段");
        }
    }

    /**
     * 校验预约日期合法且时段容量未满。
     *
     * @param slot 时段
     * @param date 预约日期
     */
    private void assertCapacityAvailable(ZjudgBookingSlot slot, LocalDate date)
    {
        int maxReserve = slot.getMaxReserve() == null ? 0 : slot.getMaxReserve();
        if (maxReserve <= 0)
        {
            throw new ServiceException("该时段暂未开放预约");
        }
        int used = mapper.countActiveBySlot(slot.getSlotId(), date.format(APPOINTMENT_FORMAT));
        if (used >= maxReserve)
        {
            throw new ServiceException("该时段已约满，请选择其他时段");
        }
    }

    /**
     * 解析并校验预约日期：不允许过去日期。
     *
     * @param appointmentDate 日期字符串
     * @return 日期
     */
    private LocalDate parseAppointmentDate(String appointmentDate)
    {
        if (StringUtils.isEmpty(appointmentDate))
        {
            throw new ServiceException("请选择预约日期");
        }
        LocalDate date;
        try
        {
            date = LocalDate.parse(appointmentDate.trim(), APPOINTMENT_FORMAT);
        }
        catch (DateTimeParseException exception)
        {
            throw new ServiceException("预约日期格式应为 yyyy-MM-dd");
        }
        if (date.isBefore(LocalDate.now()))
        {
            throw new ServiceException("不能预约过去的日期");
        }
        return date;
    }

    /**
     * 生成订单号：yyyyMMdd + 6 位随机数，冲突由唯一索引兜底。
     *
     * @return 订单号
     */
    private String nextOrderNo()
    {
        String day = new SimpleDateFormat(ORDER_NO_DATE_PATTERN).format(new Date());
        int random = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return day + random;
    }

    /**
     * 截断超长文本，避免落库失败。
     *
     * @param value 原值
     * @param maxLength 最大长度
     * @return 截断后的值
     */
    private String truncate(String value, int maxLength)
    {
        if (value == null)
        {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
