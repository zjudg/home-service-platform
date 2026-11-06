package com.zjude.system.service.impl;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.ZjudgOrder;
import com.zjude.system.domain.ZjudgServiceReview;
import com.zjude.system.mapper.ZjudgServiceReviewMapper;
import com.zjude.system.service.IZjudgOrderService;
import com.zjude.system.service.IZjudgReviewService;

/**
 * 家政服务评价服务实现。
 */
@Service("zjudgReviewService")
public class ZjudgReviewServiceImpl implements IZjudgReviewService
{
    /** 评价内容最大长度，与表结构一致 */
    private static final int CONTENT_MAX_LENGTH = 1000;

    @Resource
    private ZjudgServiceReviewMapper mapper;

    @Resource
    private IZjudgOrderService orderService;

    @Resource
    private BaseKeyGenerate keyGenerate;

    @Override
    public List<ZjudgServiceReview> selectList(ZjudgServiceReview query)
    {
        return mapper.selectList(query);
    }

    @Override
    public ZjudgServiceReview selectById(String id)
    {
        return mapper.selectById(id);
    }

    @Override
    public int insert(ZjudgServiceReview item)
    {
        item.setReviewId(keyGenerate.snowflakeStringKey("ZR_"));
        return mapper.insert(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZjudgServiceReview submitOrderReview(String orderId, Integer rating, String content,
            String userId, String userInfo)
    {
        if (StringUtils.isEmpty(userId))
        {
            throw new ServiceException("请先登录后再评价");
        }
        if (rating == null || rating < 1 || rating > 5)
        {
            throw new ServiceException("评分需在 1 到 5 之间");
        }
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isEmpty())
        {
            throw new ServiceException("请填写评价内容");
        }
        if (trimmed.length() > CONTENT_MAX_LENGTH)
        {
            throw new ServiceException("评价内容不能超过" + CONTENT_MAX_LENGTH + "个字符");
        }

        // 只能评价本人已完成订单；未完成或他人订单统一按不存在处理
        ZjudgOrder order = orderService.selectReviewableOrder(orderId, userId);
        if (order == null)
        {
            throw new ServiceException("仅可评价已完成的订单");
        }
        if (mapper.selectByOrderId(orderId) != null)
        {
            throw new ServiceException("该订单已评价，请不要重复提交");
        }

        ZjudgServiceReview review = new ZjudgServiceReview();
        review.setReviewId(keyGenerate.snowflakeStringKey("ZR_"));
        review.setOrderId(order.getOrderId());
        review.setServiceId(order.getServiceId());
        review.setUserId(userId);
        review.setUserInfo(userInfo);
        review.setRating(rating);
        review.setContent(trimmed);
        review.setReplyStatus("0");
        review.setStatus("0");
        review.setCreateBy(userId);
        mapper.insert(review);

        // 评价直接影响师傅评分，提交后立即重算，避免师傅工作台长期显示旧分数
        orderService.refreshMasterStatistics(order.getMasterId());
        return mapper.selectById(review.getReviewId());
    }

    @Override
    public int updateReply(ZjudgServiceReview item)
    {
        item.setReplyStatus(item.getReplyContent() == null || item.getReplyContent().isBlank() ? "0" : "1");
        return mapper.updateReply(item);
    }

    @Override
    public int updateStatus(ZjudgServiceReview item)
    {
        return mapper.updateStatus(item);
    }

    @Override
    public int deleteByIds(String[] ids)
    {
        return mapper.deleteByIds(ids);
    }
}
