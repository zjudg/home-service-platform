package com.zjude.web.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSONObject;
import com.zjude.common.utils.StringUtils;
import com.zjude.integrated.payment.PaymentConfigService;
import com.zjude.integrated.payment.PaymentProvider;
import com.zjude.integrated.payment.TransferConfigKeys;
import com.zjude.integrated.payment.WechatTransferClient;
import com.zjude.integrated.wechat.WechatConfigService;
import com.zjude.system.domain.ZjudgMaster;
import com.zjude.system.domain.ZjudgMasterWithdraw;
import com.zjude.system.mapper.SysUserMapper;
import com.zjude.system.mapper.ZjudgMasterMapper;
import com.zjude.system.service.IZjudgWithdrawPayer;

/**
 * 微信商家转账打款执行器。
 *
 * <p>在提现审核通过后自动向师傅微信零钱打款。是否启用由
 * {@code zjudg.settle.autoTransfer} 控制：未开通商家转账权限的商户保持关闭，
 * 由运营在微信商户平台手工转账后回填单号。</p>
 *
 * <p>金额取提现单的「实际到账」金额（已扣除手续费）。</p>
 */
@Service("zjudgWechatWithdrawPayer")
public class ZjudgWechatWithdrawPayer implements IZjudgWithdrawPayer
{
    private static final Logger log = LoggerFactory.getLogger(ZjudgWechatWithdrawPayer.class);

    /** 微信转账成功状态 */
    private static final String STATE_SUCCESS = "SUCCESS";

    /** 微信转账处理中 */
    private static final String STATE_PROCESSING = "PROCESSING";

    @Resource
    private WechatTransferClient transferClient;

    @Resource
    private WechatConfigService wechatConfigService;

    @Resource
    private PaymentConfigService paymentConfigService;

    @Resource
    private ZjudgMasterMapper masterMapper;

    @Resource
    private SysUserMapper userMapper;

    @Override
    public boolean autoEnabled()
    {
        return "1".equals(wechatConfigService.get(TransferConfigKeys.AUTO_TRANSFER_ENABLED));
    }

    @Override
    public PayoutResult payout(ZjudgMasterWithdraw withdraw)    {
        if (withdraw == null)
        {
            return PayoutResult.fail("提现单为空");
        }
        ZjudgMaster master = masterMapper.selectById(withdraw.getMasterId());
        if (master == null || StringUtils.isEmpty(master.getUserId()))
        {
            return PayoutResult.fail("师傅信息缺失");
        }
        var user = userMapper.selectUserById(master.getUserId());
        if (user == null || StringUtils.isEmpty(user.getWxOpenid()))
        {
            // 没有 openid 无法转账到零钱，转人工处理
            return PayoutResult.fail("师傅未绑定微信，无法自动转账");
        }

        long actualFen = withdraw.getActualAmount() == null ? 0L : withdraw.getActualAmount();
        if (actualFen <= 0)
        {
            return PayoutResult.fail("实际到账金额异常");
        }
        BigDecimal amountYuan = BigDecimal.valueOf(actualFen)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        try
        {
            JSONObject result = transferClient.transfer(
                    withdraw.getWithdrawNo(),
                    user.getWxOpenid(),
                    amountYuan,
                    wechatConfigService.get(TransferConfigKeys.REMARK),
                    wechatConfigService.get(TransferConfigKeys.SCENE_ID));
            String billNo = result.getString("transfer_bill_no");
            String state = result.getString("state");
            if (STATE_SUCCESS.equals(state) || "ACCEPTED".equals(state))
            {
                return PayoutResult.ok(billNo);
            }
            if (STATE_PROCESSING.equals(state) || state == null)
            {
                return PayoutResult.processing(billNo);
            }
            return PayoutResult.fail("微信转账状态：" + state);
        }
        catch (Exception exception)
        {
            log.warn("提现自动转账失败 withdrawNo={}", withdraw.getWithdrawNo(), exception);
            return PayoutResult.fail(exception.getMessage());
        }
    }

    @Override
    public PayoutResult query(ZjudgMasterWithdraw withdraw)
    {
        if (withdraw == null || StringUtils.isEmpty(withdraw.getWithdrawNo()))
        {
            return PayoutResult.processing(null);
        }
        if (!autoEnabled())
        {
            // 手工模式没有可查询的转账单，保持原状
            return PayoutResult.processing(null);
        }
        try
        {
            JSONObject result = transferClient.queryTransfer(withdraw.getWithdrawNo());
            String state = result == null ? null : result.getString("state");
            String billNo = result == null ? null : result.getString("transfer_bill_no");
            if (STATE_SUCCESS.equals(state))
            {
                return PayoutResult.ok(billNo == null ? withdraw.getWithdrawNo() : billNo);
            }
            if ("FAIL".equals(state) || "CANCELLED".equals(state) || "CLOSED".equals(state))
            {
                return PayoutResult.fail("微信转账状态：" + state);
            }
            // PROCESSING / WAIT_USER_CONFIRM / ACCEPTED 均视为处理中
            return PayoutResult.processing(billNo == null ? withdraw.getWithdrawNo() : billNo);
        }
        catch (Exception exception)
        {
            log.warn("查询转账结果失败 withdrawNo={}", withdraw.getWithdrawNo(), exception);
            return PayoutResult.processing(withdraw.getWithdrawNo());
        }
    }
}
