package com.zjude.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zjude.system.domain.ZjudgOrder;

/**
 * 家政订单数据访问。
 */
public interface ZjudgOrderMapper
{
    /**
     * 管理端条件查询。
     *
     * @param query 查询条件
     * @return 订单列表
     */
    List<ZjudgOrder> selectList(ZjudgOrder query);

    /**
     * 查询指定用户的订单列表。
     *
     * <p>供小程序用户端使用。该查询不经过管理端数据权限注入，
     * 订单归属由 user_id 显式限定。</p>
     *
     * @param userId 用户主键
     * @param orderStatus 可选订单状态
     * @return 订单列表
     */
    List<ZjudgOrder> selectListByUser(@Param("userId") String userId,
            @Param("orderStatus") String orderStatus);


    /**
     * 按主键查询。
     *
     * @param id 订单主键
     * @return 订单
     */
    ZjudgOrder selectById(String id);

    /**
     * 按主键与归属用户查询。
     *
     * @param id 订单主键
     * @param userId 用户主键
     * @return 订单
     */
    ZjudgOrder selectByIdAndUser(@Param("id") String id, @Param("userId") String userId);

    /**
     * 按主键与承接师傅查询，用于师傅端越权校验。
     *
     * @param id 订单主键
     * @param masterId 师傅主键
     * @return 订单，不属于该师傅时返回 null
     */
    ZjudgOrder selectByIdAndMaster(@Param("id") String id, @Param("masterId") String masterId);

    /**
     * 待接单池：未指派、待接单、预约日期不早于指定日期。
     *
     * @param appointmentDate 起始预约日期 yyyy-MM-dd
     * @param slotId 可选时段过滤
     * @return 待接单订单列表
     */
    List<ZjudgOrder> selectPendingPool(@Param("appointmentDate") String appointmentDate,
            @Param("slotId") String slotId);

    /**
     * 按技能过滤的待接单池。
     *
     * @param appointmentDate 起始预约日期
     * @param slotId 可选时段过滤
     * @param skillList 服务分类 ID 列表，非空
     * @return 待接单订单列表
     */
    List<ZjudgOrder> selectPendingPoolBySkills(@Param("appointmentDate") String appointmentDate,
            @Param("slotId") String slotId, @Param("skillList") java.util.List<String> skillList);

    /**
     * 统计指定时段、日期下的有效订单数（不含已取消）。
     *
     * @param slotId 时段主键
     * @param appointmentDate 预约日期 yyyy-MM-dd
     * @return 有效订单数
     */
    int countActiveBySlot(@Param("slotId") String slotId, @Param("appointmentDate") String appointmentDate);

    /**
     * 新增订单。
     *
     * @param item 订单
     * @return 影响行数
     */
    int insert(ZjudgOrder item);

    /**
     * 按主键与归属用户变更状态。
     *
     * @param id 订单主键
     * @param userId 用户主键
     * @param fromStatus 期望的当前状态
     * @param targetStatus 目标状态
     * @param operator 操作人
     * @return 影响行数
     */
    int updateStatusOwned(@Param("id") String id, @Param("userId") String userId,
            @Param("fromStatus") String fromStatus, @Param("targetStatus") String targetStatus,
            @Param("operator") String operator);

    /**
     * 管理端变更状态。
     *
     * @param id 订单主键
     * @param targetStatus 目标状态
     * @param operator 操作人
     * @return 影响行数
     */
    int updateStatus(@Param("id") String id, @Param("targetStatus") String targetStatus,
            @Param("operator") String operator);

    /**
     * 查询用户已完成订单用于评价校验。
     *
     * @param id 订单主键
     * @param userId 用户主键
     * @return 订单
     */
    ZjudgOrder selectFinishedByUser(@Param("id") String id, @Param("userId") String userId);

    /**
     * 抢单：仅当订单未指派且处于期望状态时成功，返回 0 表示已被其他师傅接走。
     *
     * @param id 订单主键
     * @param masterId 抢单师傅主键
     * @param fromStatus 期望的当前状态
     * @param targetStatus 目标状态
     * @param operator 操作人
     * @return 影响行数
     */
    int claimOrder(@Param("id") String id, @Param("masterId") String masterId,
            @Param("fromStatus") String fromStatus, @Param("targetStatus") String targetStatus,
            @Param("operator") String operator);

    /**
     * 师傅侧状态流转，同时校验订单归属与期望状态。
     *
     * @param id 订单主键
     * @param masterId 师傅主键
     * @param fromStatus 期望的当前状态
     * @param targetStatus 目标状态
     * @param operator 操作人
     * @return 影响行数
     */
    int updateStatusByMaster(@Param("id") String id, @Param("masterId") String masterId,
            @Param("fromStatus") String fromStatus, @Param("targetStatus") String targetStatus,
            @Param("operator") String operator);

    /**
     * 开工：流转状态并记录开工时间。
     *
     * @param id 订单主键
     * @param masterId 师傅主键
     * @param fromStatus 期望的当前状态
     * @param targetStatus 目标状态
     * @param operator 操作人
     * @return 影响行数
     */
    int startService(@Param("id") String id, @Param("masterId") String masterId,
            @Param("fromStatus") String fromStatus, @Param("targetStatus") String targetStatus,
            @Param("operator") String operator);

    /**
     * 完工：流转状态并记录完工时间与凭证。
     *
     * @param id 订单主键
     * @param masterId 师傅主键
     * @param fromStatus 期望的当前状态
     * @param targetStatus 目标状态
     * @param finishPhotos 完工凭证图片，逗号分隔
     * @param operator 操作人
     * @return 影响行数
     */
    int finishService(@Param("id") String id, @Param("masterId") String masterId,
            @Param("fromStatus") String fromStatus, @Param("targetStatus") String targetStatus,
            @Param("finishPhotos") String finishPhotos, @Param("operator") String operator);

    /**
     * 师傅已完工订单的平均评分。
     *
     * @param masterId 师傅主键
     * @return 平均评分，无评价时返回 null
     */
    java.math.BigDecimal selectMasterRating(@Param("masterId") String masterId);

    /**
     * 师傅累计完成订单数。
     *
     * @param masterId 师傅主键
     * @return 完单数
     */
    int countFinishedByMaster(@Param("masterId") String masterId);

    /**
     * 标记订单已支付。仅当订单仍未支付时生效，保证回调幂等。
     *
     * @param orderId 订单主键
     * @param operator 操作人
     * @return 影响行数
     */
    int markPaid(@Param("orderId") String orderId, @Param("operator") String operator);

    /**
     * 标记订单已退款。仅当订单处于已支付时生效。
     *
     * @param orderId 订单主键
     * @param operator 操作人
     * @return 影响行数
     */
    int markRefunded(@Param("orderId") String orderId, @Param("operator") String operator);

    /**
     * 标记订单已结算，并记录师傅收入与平台佣金。
     *
     * <p>仅当仍处于未结算状态时生效，保证同一订单只结算一次。</p>
     *
     * @param orderId 订单主键
     * @param masterIncome 师傅收入（分）
     * @param platformFee 平台佣金（分）
     * @return 影响行数
     */
    int markSettled(@Param("orderId") String orderId, @Param("masterIncome") long masterIncome,
            @Param("platformFee") long platformFee);

    /**
     * 标记订单结算已冲回。
     *
     * @param orderId 订单主键
     * @return 影响行数
     */
    int markSettleReversed(@Param("orderId") String orderId);

    /**
     * 指派师傅。
     *
     * <p>条件保证并发下同一订单只会被指派一次；{@code expectDispatchStatus} 非空时
     * 还会校验当前派单状态，用于自动派单的抢占。</p>
     *
     * @param orderId 订单主键
     * @param masterId 师傅主键
     * @param expectDispatchStatus 期望的派单状态，为空则不校验
     * @return 影响行数
     */
    int assignMaster(@Param("orderId") String orderId, @Param("masterId") String masterId,
            @Param("expectDispatchStatus") String expectDispatchStatus);

    /**
     * 释放订单的承接师傅，退回待派单状态。
     *
     * <p>用于师傅拒单：只有「该师傅仍持有此单且订单未开工」时才允许释放，
     * 防止拒单把已经开工或已换人的订单误清空。</p>
     *
     * @param orderId 订单主键
     * @param masterId 师傅主键
     * @return 影响行数
     */
    int releaseMaster(@Param("orderId") String orderId, @Param("masterId") String masterId);

    /**
     * 标记派单失败。
     *
     * @param orderId 订单主键
     * @return 影响行数
     */
    int markDispatchFailed(@Param("orderId") String orderId);

    /**
     * 查询待派单订单：已支付、待接单、未指派、未派单成功。
     *
     * @return 订单列表
     */
    List<ZjudgOrder> selectDispatchPending();
}
