package com.zjude.system.service;

import java.util.List;
import com.zjude.system.domain.ZjudgOrder;

/**
 * 家政服务订单服务。
 *
 * <p>写操作均需在应用服务边界保证事务与业务不变量：
 * 金额以服务端价格计算、时段容量受 max_reserve 约束、状态按
 * {@link com.zjude.common.enums.ZjudgOrderStatus} 规则流转。</p>
 */
public interface IZjudgOrderService
{
    /**
     * 管理端查询订单列表。
     *
     * @param query 查询条件
     * @return 订单列表
     */
    List<ZjudgOrder> selectList(ZjudgOrder query);

    /** 管理端条件查询，允许查看完整用户昵称。 */
    List<ZjudgOrder> selectAdminList(ZjudgOrder query);

    /** 管理后台订单详情，允许查看用户完整昵称。 */
    ZjudgOrder selectAdminById(String id);

    /** 查询当前用户订单列表，不使用管理端数据权限过滤。 */
    List<ZjudgOrder> selectListByUser(String userId, String orderStatus);

    /**
     * 按主键查询订单。
     *
     * @param id 订单主键
     * @return 订单
     */
    ZjudgOrder selectById(String id);

    /**
     * 查询指定用户在指定日期、时段下的有效订单数量。
     *
     * @param slotId 时段主键
     * @param appointmentDate 预约日期 yyyy-MM-dd
     * @return 有效订单数量
     */
    int countActiveBySlot(String slotId, String appointmentDate);

    /**
     * 小程序下单。
     *
     * @param serviceId 服务主键
     * @param slotId 预约时段主键
     * @param appointmentDate 预约日期 yyyy-MM-dd
     * @param remark 备注
     * @param address 服务地址
     * @param phone 联系电话
     * @param userId 下单用户主键
     * @param userInfo 用户脱敏展示信息
     * @return 新建订单
     */

    /**
     * 创建订单并记录下单城市。
     *
     * <p>城市归属优先取服务发布城市；服务为全国时使用这里传入的城市。</p>
     *
     * @param serviceId 服务主键
     * @param slotId 预约时段主键
     * @param appointmentDate 预约日期 yyyy-MM-dd
     * @param remark 备注
     * @param address 服务地址
     * @param phone 联系电话
     * @param userId 下单用户主键
     * @param userInfo 用户脱敏展示信息
     * @param cityId 下单城市主键，可为空
     * @return 新建订单
     */

    /** 小程序下单，计量方式与校验规则由服务单位后台配置决定。 */
    ZjudgOrder createOrder(String serviceId, String slotId, String appointmentDate, java.math.BigDecimal quantity,
            String remark, String address, String phone, String userId, String userInfo, String cityId);

    /** 管理端录入已在线下收款的订单，订单直接进入师傅接单大厅。 */
    ZjudgOrder createOfflinePaidOrder(String serviceId, String slotId, String appointmentDate,
            java.math.BigDecimal quantity, String remark, String address, String phone, String customerName,
            String cityId, String operator);

    /**
     * 取消订单，仅允许本人操作且订单未终结。
     *
     * @param orderId 订单主键
     * @param userId 操作人用户主键
     * @return 变更影响行数
     */
    int cancelOrder(String orderId, String userId);

    /**
     * 确认订单完成，仅允许本人操作且订单处于待确认或服务中。
     *
     * @param orderId 订单主键
     * @param userId 操作人用户主键
     * @return 变更影响行数
     */
    int confirmOrder(String orderId, String userId);

    /**
     * 按主键与归属用户查询订单，用于越权校验。
     *
     * @param orderId 订单主键
     * @param userId 用户主键
     * @return 订单，不存在或不属于该用户时返回 null
     */
    ZjudgOrder selectByIdAndUser(String orderId, String userId);

    /**
     * 管理端流转订单状态。
     *
     * @param orderId 订单主键
     * @param target 目标状态编码
     * @param operator 操作人
     * @return 变更影响行数
     */
    int updateStatus(String orderId, String target, String operator);

    /**
     * 删除未支付、未指派且没有关联履约或财务记录的已取消订单。
     *
     * @param query 订单主键与数据权限过滤条件
     * @return 影响行数
     */
    int deleteAdminOrder(ZjudgOrder query);

    /** 删除当前用户符合条件的已取消订单。 */
    int deleteUserOrder(String orderId, String userId);

    /**
     * 查询用户在指定服务下已完成且尚未评价的订单。
     *
     * @param orderId 订单主键
     * @param userId 用户主键
     * @return 可评价订单，不可评价时返回 null
     */
    ZjudgOrder selectReviewableOrder(String orderId, String userId);

    /**
     * 待接单池。仅返回未指派、待接单且预约日期不早于今天的订单。
     *
     * @param slotId 可选时段过滤
     * @return 待接单订单列表
     */
    List<ZjudgOrder> selectPendingPool(String slotId);

    /**
     * 待接单池，可按师傅技能过滤。
     *
     * <p>师傅自选「只看匹配」时才启用技能过滤；传空或关闭时返回全部待接单订单。</p>
     *
     * @param slotId 可选时段过滤
     * @param masterId 师傅主键，用于取技能
     * @param onlyMatched 是否仅返回技能匹配的订单
     * @return 待接单订单列表
     */
    List<ZjudgOrder> selectPendingPool(String slotId, String masterId, boolean onlyMatched);

    /**
     * 师傅的订单列表。
     *
     * @param masterId 师傅主键
     * @param orderStatus 可选状态过滤
     * @return 订单列表
     */
    List<ZjudgOrder> selectByMaster(String masterId, String orderStatus);

    /**
     * 师傅查看自己的订单详情。
     *
     * @param orderId 订单主键
     * @param masterId 师傅主键
     * @return 订单，不属于该师傅时返回 null
     */
    ZjudgOrder selectForMaster(String orderId, String masterId);

    /**
     * 抢单。并发下仅一位师傅成功，其余抛业务异常。
     *
     * @param orderId 订单主键
     * @param masterId 抢单师傅主键
     * @return 影响行数
     */
    int claimOrder(String orderId, String masterId);

    /**
     * 师傅开工。
     *
     * @param orderId 订单主键
     * @param masterId 师傅主键
     * @return 影响行数
     */
    int startService(String orderId, String masterId);

    /**
     * 师傅完工，并回写师傅评分与完单数。
     *
     * @param orderId 订单主键
     * @param masterId 师傅主键
     * @param finishPhotos 完工凭证图片，逗号分隔
     * @return 影响行数
     */
    int finishService(String orderId, String masterId, String finishPhotos);

    /**
     * 重算师傅的评分与完单数。
     *
     * <p>订单进入「已完成」或用户提交评价后都必须调用一次，
     * 否则 {@code zjudg_master.rating} 会长期停在上一次计算的旧值。</p>
     *
     * @param masterId 师傅主键，为空时不做处理
     */
    void refreshMasterStatistics(String masterId);
}
