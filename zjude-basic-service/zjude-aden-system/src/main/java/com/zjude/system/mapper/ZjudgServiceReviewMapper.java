package com.zjude.system.mapper;

import java.util.List;
import com.zjude.system.domain.ZjudgServiceReview;

/**
 * 家政服务评价数据访问。
 */
public interface ZjudgServiceReviewMapper
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
     * 按订单查询评价，用于防止重复评价。
     *
     * @param orderId 订单主键
     * @return 评价，不存在时返回 null
     */
    ZjudgServiceReview selectByOrderId(String orderId);

    /**
     * 新增评价。
     *
     * @param item 评价
     * @return 影响行数
     */
    int insert(ZjudgServiceReview item);

    /**
     * 更新商家回复。
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
