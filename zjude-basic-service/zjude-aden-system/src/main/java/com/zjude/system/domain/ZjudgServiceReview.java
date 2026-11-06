package com.zjude.system.domain;

import java.io.Serial;
import com.zjude.common.core.domain.BaseEntity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** 家政服务评价。 */
public class ZjudgServiceReview extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    private String reviewId;
    private String orderId;
    private String serviceId;
    private String serviceName;
    private String userId;
    private String userInfo;
    private Integer rating;
    private String content;
    private String replyContent;
    private String replyStatus;
    private String status;
    public String getReviewId() { return reviewId; }
    public void setReviewId(String value) { reviewId = value; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { orderId = value; }
    public String getServiceId() { return serviceId; }
    public void setServiceId(String value) { serviceId = value; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String value) { serviceName = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { userId = value; }
    public String getUserInfo() { return userInfo; }
    public void setUserInfo(String value) { userInfo = value; }
    @Min(value = 1, message = "评分最低为1") @Max(value = 5, message = "评分最高为5") public Integer getRating() { return rating; }
    public void setRating(Integer value) { rating = value; }
    @NotBlank(message = "评价内容不能为空") public String getContent() { return content; }
    public void setContent(String value) { content = value; }
    public String getReplyContent() { return replyContent; }
    public void setReplyContent(String value) { replyContent = value; }
    public String getReplyStatus() { return replyStatus; }
    public void setReplyStatus(String value) { replyStatus = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
}
