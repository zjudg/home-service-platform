package com.zjude.system.domain;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjude.common.core.domain.BaseEntity;

/** 家政服务订单查询模型。 */
public class ZjudgOrder extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    private String orderId;
    private String orderNo;
    private String serviceId;
    private String serviceName;
    /** Service cover used by customer order cards; not stored in the order row. */
    private String serviceImage;
    /** Category snapshot used by compact customer order summaries. */
    private String categoryName;
    /** Category icon exposed for the customer's recent-order summary. */
    private String categoryImage;
    /** 订单所属城市ID */
    private String cityId;
    private String userId;
    private String userInfo;
    /** 承接师傅ID，为空表示待接单 */
    private String masterId;
    /** 派单状态（0未派单 1已派单 2派单失败） */
    private String dispatchStatus;
    /** 派单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date dispatchTime;
    /** 师傅接单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date acceptTime;
    /** 师傅开工时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date startTime;
    /** 师傅完工时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date finishTime;
    /** 完工凭证图片地址，逗号分隔 */
    private String finishPhotos;
    @JsonFormat(pattern = "yyyy-MM-dd") private Date appointmentDate;
    private String slotId;
    private String slotName;
    /** 服务计量数量，由服务单位的后台计量方式决定。 */
    private BigDecimal quantity;
    /** 服务计量单位快照。 */
    private String quantityUnit;
    /** 服务地址（用户下单时填写） */
    private String address;
    /** 联系电话（用户下单时填写） */
    private String phone;
    private BigDecimal amount;
    private String orderStatus;
    private String paymentStatus;
    /** 结算状态（0未结算 1已结算 2已冲回） */
    private String settleStatus;
    /** 师傅本单收入（分） */
    private Long masterIncome;
    /** 平台佣金（分） */
    private Long platformFee;
    /** 结算时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date settleTime;
    /** 该订单是否已提交评价（0 未评价 1 已评价） */
    private Integer reviewed;
    public String getOrderId() { return orderId; }
    public void setOrderId(String value) { orderId = value; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { orderNo = value; }
    public String getServiceId() { return serviceId; }
    public void setServiceId(String value) { serviceId = value; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String value) { serviceName = value; }
    public String getServiceImage() { return serviceImage; }
    public void setServiceImage(String value) { serviceImage = value; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String value) { categoryName = value; }
    public String getCategoryImage() { return categoryImage; }
    public void setCategoryImage(String value) { categoryImage = value; }
    public String getCityId() { return cityId; }
    public void setCityId(String value) { cityId = value; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { userId = value; }
    public String getUserInfo() { return userInfo; }
    public void setUserInfo(String value) { userInfo = value; }
    public String getMasterId() { return masterId; }
    public void setMasterId(String value) { masterId = value; }
    public String getDispatchStatus() { return dispatchStatus; }
    public void setDispatchStatus(String value) { dispatchStatus = value; }
    public Date getDispatchTime() { return dispatchTime; }
    public void setDispatchTime(Date value) { dispatchTime = value; }
    public String getSettleStatus() { return settleStatus; }
    public void setSettleStatus(String value) { settleStatus = value; }
    public Long getMasterIncome() { return masterIncome == null ? 0L : masterIncome; }
    public void setMasterIncome(Long value) { masterIncome = value; }
    public Long getPlatformFee() { return platformFee == null ? 0L : platformFee; }
    public void setPlatformFee(Long value) { platformFee = value; }
    public Date getSettleTime() { return settleTime; }
    public void setSettleTime(Date value) { settleTime = value; }
    public Date getAcceptTime() { return acceptTime; }
    public void setAcceptTime(Date value) { acceptTime = value; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date value) { startTime = value; }
    public Date getFinishTime() { return finishTime; }
    public void setFinishTime(Date value) { finishTime = value; }
    public String getFinishPhotos() { return finishPhotos; }
    public void setFinishPhotos(String value) { finishPhotos = value; }
    public Date getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(Date value) { appointmentDate = value; }
    public String getSlotId() { return slotId; }
    public void setSlotId(String value) { slotId = value; }
    public String getSlotName() { return slotName; }
    public void setSlotName(String value) { slotName = value; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal value) { quantity = value; }
    public String getQuantityUnit() { return quantityUnit; }
    public void setQuantityUnit(String value) { quantityUnit = value; }
    public String getAddress() { return address; }
    public void setAddress(String value) { address = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { phone = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String value) { orderStatus = value; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String value) { paymentStatus = value; }
    public Integer getReviewed() { return reviewed; }
    public void setReviewed(Integer value) { reviewed = value; }
}
