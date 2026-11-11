package com.zjude.web.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.integrated.payment.PaymentConfig;
import com.zjude.integrated.payment.PaymentConfigService;
import com.zjude.integrated.payment.PaymentProvider;
import com.zjude.integrated.payment.WechatPayClient;
import com.zjude.system.domain.ZjudgBusinessPayment;
import com.zjude.system.domain.ZjudgCertificateApplication;
import com.zjude.system.domain.ZjudgCertificateProduct;
import com.zjude.system.domain.ZjudgOrderExtraTime;
import com.zjude.system.mapper.SysUserMapper;
import com.zjude.system.mapper.ZjudgBusinessPaymentMapper;
import com.zjude.system.mapper.ZjudgCertificateApplicationMapper;
import com.zjude.system.mapper.ZjudgCertificateProductMapper;
import com.zjude.system.service.IZjudgBusinessPaymentService;
import com.zjude.system.service.IZjudgOrderExtraTimeService;
import com.zjude.system.domain.enums.ZjudgBusinessPaymentStatus;
import com.zjude.system.domain.enums.ZjudgCertificateApplicationStatus;
import com.zjude.system.domain.enums.ZjudgCertificateProductStatus;
import com.zjude.system.domain.enums.ZjudgExtraTimePaymentStatus;

/** 证书申请和补时订单共用支付流水，但业务状态彼此独立。 */
@Service("zjudgBusinessPaymentService")
public class ZjudgBusinessPaymentServiceImpl implements IZjudgBusinessPaymentService
{
    private static final String TRADE_SUCCESS = "SUCCESS";
    @Resource private IZjudgOrderExtraTimeService extraTimeService;
    @Resource private ZjudgCertificateProductMapper productMapper;
    @Resource private ZjudgCertificateApplicationMapper applicationMapper;
    @Resource private ZjudgBusinessPaymentMapper paymentMapper;
    @Resource private SysUserMapper userMapper;
    @Resource private BaseKeyGenerate keyGenerate;
    @Resource private WechatPayClient wechatPayClient;
    @Resource private PaymentConfigService paymentConfigService;
    @Resource private WechatNotifyVerifier notifyVerifier;

    public List<ZjudgCertificateProduct> listProducts() { return productMapper.selectPublishedList(); }
    public List<ZjudgCertificateProduct> listAllProducts(ZjudgCertificateProduct query) { return productMapper.selectList(query); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZjudgCertificateApplication createCertificateApplication(String productId, String userId, String phone, String photoUrl, String address)
    {
        if (StringUtils.isEmpty(userId)) throw new ServiceException("请先登录");
        ZjudgCertificateProduct product = productMapper.selectById(productId);
        if (product == null || !ZjudgCertificateProductStatus.ON_SALE.value().equals(product.getStatus())) throw new ServiceException("证书商品不存在或已下架");
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(address)) throw new ServiceException("请填写联系电话和快递地址");
        if (StringUtils.isEmpty(photoUrl)) throw new ServiceException("请上传办理所需电子照片");
        ZjudgCertificateApplication item = new ZjudgCertificateApplication();
        item.setApplicationId(keyGenerate.snowflakeStringKey("ZCA_")); item.setApplicationNo(keyGenerate.snowflakeStringKey("ZCAN_"));
        item.setProductId(productId); item.setUserId(userId); item.setContactPhone(phone.trim()); item.setPhotoUrl(photoUrl); item.setDeliveryAddress(address.trim()); item.setAmount(product.getPrice()); item.setStatus(ZjudgCertificateApplicationStatus.PENDING_PAYMENT.value());
        applicationMapper.insert(item);
        return applicationMapper.selectById(item.getApplicationId());
    }
    public List<ZjudgCertificateApplication> listApplications() { return applicationMapper.selectList(new ZjudgCertificateApplication()); }
    public ZjudgCertificateApplication getApplicationForUser(String id, String userId) { return applicationMapper.selectByIdAndUser(id, userId); }
    public int updateApplication(String id, String status, String trackingNo)
    {
        if (StringUtils.isEmpty(status) || (!ZjudgCertificateApplicationStatus.PENDING_REVIEW.value().equals(status)
                && !ZjudgCertificateApplicationStatus.PROCESSING.value().equals(status)
                && !ZjudgCertificateApplicationStatus.SHIPPED.value().equals(status)
                && !ZjudgCertificateApplicationStatus.COMPLETED.value().equals(status))) throw new ServiceException("证书申请状态不合法");
        if (ZjudgCertificateApplicationStatus.SHIPPED.value().equals(status) && StringUtils.isEmpty(trackingNo)) throw new ServiceException("寄出证书时必须填写快递单号");
        ZjudgCertificateApplication application = applicationMapper.selectById(id);
        if (application == null) throw new ServiceException("证书申请不存在");
        String current = application.getStatus();
        boolean transitionAllowed = ZjudgCertificateApplicationStatus.PENDING_REVIEW.value().equals(current)
                ? ZjudgCertificateApplicationStatus.PENDING_REVIEW.value().equals(status)
                    || ZjudgCertificateApplicationStatus.PROCESSING.value().equals(status)
                    || ZjudgCertificateApplicationStatus.SHIPPED.value().equals(status)
                : ZjudgCertificateApplicationStatus.PROCESSING.value().equals(current)
                    ? ZjudgCertificateApplicationStatus.PROCESSING.value().equals(status)
                        || ZjudgCertificateApplicationStatus.SHIPPED.value().equals(status)
                    : ZjudgCertificateApplicationStatus.SHIPPED.value().equals(current)
                        ? ZjudgCertificateApplicationStatus.SHIPPED.value().equals(status)
                            || ZjudgCertificateApplicationStatus.COMPLETED.value().equals(status)
                        : ZjudgCertificateApplicationStatus.COMPLETED.value().equals(current)
                            && ZjudgCertificateApplicationStatus.COMPLETED.value().equals(status);
        if (!transitionAllowed) throw new ServiceException("证书申请状态不能从当前状态变更到目标状态");
        int updated = applicationMapper.updateFulfillment(id, current,
                StringUtils.isEmpty(trackingNo) ? null : trackingNo.trim(), status);
        if (updated == 0) throw new ServiceException("申请状态已被其他管理员更新，请刷新后重试");
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPayment(String bizType, String bizId, String userId)
    {
        BigDecimal amount;
        String title;
        if ("C".equals(bizType))
        {
            ZjudgCertificateApplication app = getApplicationForUser(bizId, userId); if (app == null) throw new ServiceException("证书申请不存在");
            if (!ZjudgCertificateApplicationStatus.PENDING_PAYMENT.value().equals(app.getStatus())) throw new ServiceException("当前证书申请不可支付"); amount = app.getAmount(); title = app.getProductName();
        }
        else if ("T".equals(bizType))
        {
            ZjudgOrderExtraTime extraTime = extraTimeService.getForUser(bizId, userId);
            if (extraTime == null) throw new ServiceException("补时记录不存在");
            if (!ZjudgExtraTimePaymentStatus.PENDING.value().equals(extraTime.getPaymentStatus())) throw new ServiceException("该补时记录已支付");
            amount = extraTime.getAmount(); title = extraTime.getServiceName() + "补时" + extraTime.getHours() + "小时";
        }
        else throw new ServiceException("支付业务类型不支持");
        var user = userMapper.selectUserById(userId); if (user == null || StringUtils.isEmpty(user.getWxOpenid())) throw new ServiceException("未获取到微信身份，请重新登录后再支付");
        ZjudgBusinessPayment payment = paymentMapper.selectPending(bizType, bizId, userId);
        if (payment == null)
        {
            payment = new ZjudgBusinessPayment(); payment.setPaymentId(keyGenerate.snowflakeStringKey("ZBP_")); payment.setBizType(bizType); payment.setBizId(bizId); payment.setUserId(userId); payment.setOutTradeNo(keyGenerate.snowflakeStringKey("ZBT_")); payment.setAmount(amount); payment.setPayStatus(ZjudgBusinessPaymentStatus.PENDING.value()); paymentMapper.insert(payment);
        }
        Map<String, Object> params = wechatPayClient.createJsapiOrder(title, payment.getOutTradeNo(), amount, user.getWxOpenid());
        if (params.get("prepayId") != null) paymentMapper.updatePrepayId(payment.getOutTradeNo(), String.valueOf(params.get("prepayId")));
        return params;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean syncPayment(String bizType, String bizId, String userId)
    {
        ZjudgBusinessPayment payment = paymentMapper.selectLatest(bizType, bizId, userId);
        if (payment == null) return false;
        if (ZjudgBusinessPaymentStatus.PAID.value().equals(payment.getPayStatus())) return true;
        return confirmByWechat(payment.getOutTradeNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmByWechat(String outTradeNo)
    {
        ZjudgBusinessPayment payment = paymentMapper.selectByOutTradeNo(outTradeNo);
        if (payment == null) return false;
        JSONObject result = wechatPayClient.queryByOutTradeNo(outTradeNo);
        if (result == null || !TRADE_SUCCESS.equals(result.getString("trade_state"))) return false;
        PaymentConfig config = paymentConfigService.get(PaymentProvider.WECHAT);
        if (!java.util.Objects.equals(config.appId(), result.getString("appid"))
                || !java.util.Objects.equals(config.mchId(), result.getString("mchid"))
                || !java.util.Objects.equals(outTradeNo, result.getString("out_trade_no")))
        {
            throw new ServiceException("微信支付订单信息校验失败");
        }
        JSONObject paidAmount = result.getJSONObject("amount");
        Integer totalFen = paidAmount == null ? null : paidAmount.getInteger("total");
        int expectedFen = payment.getAmount().multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.UNNECESSARY).intValueExact();
        String currency = paidAmount == null ? null : paidAmount.getString("currency");
        if (!Integer.valueOf(expectedFen).equals(totalFen) || !"CNY".equals(currency))
        {
            throw new ServiceException("微信支付金额校验失败");
        }
        if (StringUtils.isEmpty(result.getString("transaction_id"))) throw new ServiceException("微信支付单缺少交易号");
        int changed = paymentMapper.markPaid(outTradeNo, result.getString("transaction_id"));
        if (changed == 0) return true;
        int businessChanged;
        if ("C".equals(payment.getBizType()))
        {
            businessChanged = applicationMapper.updateStatus(payment.getBizId(), ZjudgCertificateApplicationStatus.PENDING_PAYMENT.value(), ZjudgCertificateApplicationStatus.PENDING_REVIEW.value());
        }
        else if ("T".equals(payment.getBizType()))
        {
            businessChanged = extraTimeService.markPaidAndSettle(payment.getBizId()) ? 1 : 0;
        }
        else
        {
            throw new ServiceException("支付业务类型不支持");
        }
        if (businessChanged == 0) throw new ServiceException("支付成功但业务状态更新失败，请联系管理员核查");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleNotify(String body, String serial, String nonce, String signature, String timestamp)
    {
        if (body == null || body.isBlank()) return false;
        PaymentConfig config = paymentConfigService.get(PaymentProvider.WECHAT);
        boolean verified = notifyVerifier.verify(config, body, serial, nonce, signature, timestamp);
        JSONObject payload = JSON.parseObject(body); JSONObject resource = payload == null ? null : payload.getJSONObject("resource");
        String plain = verified ? notifyVerifier.decrypt(config, resource) : null;
        if (plain == null) return false;
        JSONObject trade = JSON.parseObject(plain); String outTradeNo = trade == null ? null : trade.getString("out_trade_no");
        if (paymentMapper.selectByOutTradeNo(outTradeNo) == null) return false;
        if (!TRADE_SUCCESS.equals(trade.getString("trade_state"))) return true;
        confirmByWechat(outTradeNo); return true;
    }

}
