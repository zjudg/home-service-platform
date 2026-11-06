package com.zjude.system.service;

import java.util.List;
import com.zjude.system.domain.ZjudgServiceReview;

/**
 * 家政服务评价服务。
 */
public interface IZjudgReviewService
{
    /**
     * 条件查询评价列表。
     *
     * @param query 查询条件
     * @return 评价列表
     */
    List<ZjudgServiceReview> selectList(ZjudgServiceReview query);

    /**
     * 按主键查询。
     *
     * @param id 评价主键
     * @return 评价
     */
    ZjudgServiceReview selectById(String id);

    /**
     * 管理端新增评价。
     *
     * @param item 评价
     * @return 影响行数
     */
    int insert(ZjudgServiceReview item);

    /**
     * 用户提交订单评价。
     *
     * <p>仅允许评价本人已完成且未评价过的订单，服务与用户信息由订单快照决定。</p>
     *
     * @param orderId 订单主键
     * @param rating 评分 1-5
     * @param content 评价内容
     * @param userId 当前用户主键
     * @param userInfo 用户脱敏展示信息
     * @return 新建评价
     */
    ZjudgServiceReview submitOrderReview(String orderId, Integer rating, String content, String userId, String userInfo);

    /**
     * 商家回复评价。
     *
     * @param item 评价
     * @return 影响行数
     */
    int updateReply(ZjudgServiceReview item);

    /**
     * 更新展示状态。
     *
     * @param item 评价
     * @return 影响行数
     */
    int updateStatus(ZjudgServiceReview item);

    /**
     * 批量删除。
     *
     * @param ids 主键数组
     * @return 影响行数
     */
    int deleteByIds(String[] ids);
}
