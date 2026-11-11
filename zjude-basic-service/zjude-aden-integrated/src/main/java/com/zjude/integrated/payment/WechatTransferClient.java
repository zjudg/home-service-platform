package com.zjude.integrated.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;

/**
 * 微信商家转账（提现打款）客户端。
 *
 * <p>对应微信「商家转账到零钱」v3 接口。与普通支付不同，转账一旦成功不可撤回，
 * 因此调用方必须在余额已冻结的前提下发起，且转账单号需保证全局唯一以便幂等。</p>
 *
 * <p>未开通该权限的商户会把 {@link TransferConfigKeys#AUTO_TRANSFER_ENABLED} 保持为 0，
 * 此时由后台手工打款，本客户端不会被调用。</p>
 */
@Service
public class WechatTransferClient
{
    private static final Logger log = LoggerFactory.getLogger(WechatTransferClient.class);

    private static final String TRANSFER_PATH = "/v3/fund-app/mch-transfer/transfer-bills";

    private static final String TRANSFER_QUERY_PATH = "/v3/fund-app/mch-transfer/transfer-bills/out-bill-no";

    private static final String API_HOST = "https://api.mch.weixin.qq.com";

    @Resource
    private PaymentConfigService configService;

    @Resource
    private WechatPaySigner signer;

    /**
     * 发起商家转账。
     *
     * @param outBillNo 商户转账单号，需全局唯一
     * @param openid 收款人小程序 openid
     * @param amountYuan 转账金额（元）
     * @param remark 转账备注
     * @param sceneId 转账场景 ID
     * @return 微信返回结果，含 transfer_bill_no、state 等
     */
    public JSONObject transfer(String outBillNo, String openid, BigDecimal amountYuan, String remark, String sceneId)
    {
        PaymentConfig config = configService.get(PaymentProvider.WECHAT);
        requireTransferReady(config);
        require(outBillNo, "转账单号");
        require(openid, "收款人 openid");
        if (amountYuan == null || amountYuan.signum() <= 0)
        {
            throw new ServiceException("转账金额必须大于 0");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("appid", config.appId());
        body.put("out_bill_no", outBillNo);
        body.put("transfer_scene_id", StringUtils.isEmpty(sceneId) ? "1000" : sceneId);
        body.put("openid", openid);
        body.put("transfer_amount", toFen(amountYuan));
        body.put("transfer_remark", StringUtils.isEmpty(remark) ? "佣金结算" : truncate(remark, 32));
        body.put("transfer_scene_report_infos", defaultSceneReports());

        String payload = JSON.toJSONString(body);
        String nonce = UUID.randomUUID().toString().replace("-", "");
        long timestamp = System.currentTimeMillis() / 1000;
        try
        {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(API_HOST + TRANSFER_PATH))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "zjude-super-mall/1.0")
                    .header("Authorization", signer.authorization(config, "POST", TRANSFER_PATH, payload, nonce, timestamp))
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                String message = extractMessage(response.body());
                log.warn("微信转账失败 HTTP {} message={}", response.statusCode(), message);
                throw new ServiceException("微信转账失败：" + message);
            }
            JSONObject result = JSON.parseObject(response.body());
            if (result == null || StringUtils.isEmpty(result.getString("transfer_bill_no")))
            {
                throw new ServiceException("微信转账返回缺少转账单号");
            }
            return result;
        }
        catch (ServiceException exception)
        {
            throw exception;
        }
        catch (InterruptedException exception)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("微信转账被中断");
        }
        catch (Exception exception)
        {
            log.warn("微信转账异常 outBillNo={}", outBillNo, exception);
            throw new ServiceException("微信转账失败，请检查商户私钥和网络配置");
        }
    }

    /**
     * 查询转账结果。
     *
     * @param outBillNo 商户转账单号
     * @return 查询结果，含 state 字段
     */
    public JSONObject queryTransfer(String outBillNo)
    {
        PaymentConfig config = configService.get(PaymentProvider.WECHAT);
        requireTransferReady(config);
        require(outBillNo, "转账单号");

        String path = TRANSFER_QUERY_PATH + "/" + java.net.URLEncoder.encode(outBillNo, StandardCharsets.UTF_8);
        String nonce = UUID.randomUUID().toString().replace("-", "");
        long timestamp = System.currentTimeMillis() / 1000;
        try
        {
            HttpClient client = HttpClient.newHttpClient();
            String authorization = signer.authorization(config, "GET", path, "", nonce, timestamp);
            HttpRequest request = HttpRequest.newBuilder(URI.create(API_HOST + path))
                    .header("Accept", "application/json")
                    .header("User-Agent", "zjude-super-mall/1.0")
                    .header("Authorization", authorization)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw new ServiceException("微信转账查询失败，HTTP " + response.statusCode());
            }
            return JSON.parseObject(response.body());
        }
        catch (ServiceException exception)
        {
            throw exception;
        }
        catch (InterruptedException exception)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("微信转账查询被中断");
        }
        catch (Exception exception)
        {
            throw new ServiceException("微信转账查询失败，请检查商户私钥和网络配置");
        }
    }

    /**
     * 校验转账所需配置是否齐全。
     *
     * @param config 支付配置
     */
    private void requireTransferReady(PaymentConfig config)
    {
        signer.requireReady(config);
    }

    /**
     * 转账场景报备信息，微信要求至少填写一组。
     *
     * @return 场景报备
     */
    private java.util.List<Map<String, String>> defaultSceneReports()
    {
        Map<String, String> item = new LinkedHashMap<>();
        item.put("info_type", "岗位类型");
        item.put("info_content", "家政服务人员");
        return java.util.List.of(item);
    }

    private int toFen(BigDecimal amount)
    {
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    private String truncate(String value, int max)
    {
        return value.length() <= max ? value : value.substring(0, max);
    }

    private String extractMessage(String body)
    {
        if (StringUtils.isEmpty(body))
        {
            return "未知错误";
        }
        try
        {
            JSONObject parsed = JSON.parseObject(body);
            String message = parsed == null ? null : parsed.getString("message");
            return StringUtils.isEmpty(message) ? "未知错误" : message;
        }
        catch (Exception exception)
        {
            return "未知错误";
        }
    }

    private void require(String value, String name)
    {
        if (StringUtils.isEmpty(value))
        {
            throw new ServiceException(name + "未配置");
        }
    }
}
