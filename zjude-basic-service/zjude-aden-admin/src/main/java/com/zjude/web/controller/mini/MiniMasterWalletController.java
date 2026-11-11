package com.zjude.web.controller.mini;

import java.util.List;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.utils.SecurityUtils;
import com.zjude.system.domain.ZjudgMasterWallet;
import com.zjude.system.domain.ZjudgMasterWalletLog;
import com.zjude.system.domain.ZjudgMasterWithdraw;
import com.zjude.system.service.IZjudgMasterService;
import com.zjude.system.service.IZjudgMasterWithdrawService;
import com.zjude.system.service.IZjudgSettleService;

/**
 * 小程序师傅钱包与提现接口。
 *
 * <p>所有接口先经 {@link IZjudgMasterService#requireActiveMaster} 校验身份，
 * 金额对外统一以「元」的字符串返回，避免前端浮点误差。</p>
 */
@RestController
@RequestMapping("/api/mini/master/wallet")
public class MiniMasterWalletController extends BaseController
{
    @Resource
    private IZjudgSettleService settleService;

    @Resource
    private IZjudgMasterWithdrawService withdrawService;

    @Resource
    private IZjudgMasterService masterService;

    /**
     * 我的钱包。
     *
     * @return 余额、冻结、累计收入与累计提现（均为元）
     */
    @GetMapping
    public AjaxResult wallet()
    {
        String masterId = masterService.requireActiveMaster(SecurityUtils.getUserId());
        ZjudgMasterWallet wallet = settleService.getWallet(masterId);
        AjaxResult result = AjaxResult.success();
        result.put("balance", yuan(wallet.getBalance()));
        result.put("frozen", yuan(wallet.getFrozen()));
        result.put("totalIncome", yuan(wallet.getTotalIncome()));
        result.put("totalWithdraw", yuan(wallet.getTotalWithdraw()));
        return result;
    }

    /**
     * 钱包流水。
     *
     * @param bizType 可选业务类型过滤
     * @return 流水列表
     */
    @GetMapping("/logs")
    public AjaxResult logs(@RequestParam(required = false) String bizType)
    {
        String masterId = masterService.requireActiveMaster(SecurityUtils.getUserId());
        List<ZjudgMasterWalletLog> logs = settleService.listLogs(masterId, bizType);
        return AjaxResult.success(toLogViews(logs));
    }

    /**
     * 提交提现申请。
     *
     * @param body 金额与收款账号
     * @return 提现单
     */
    @PostMapping("/withdraw")
    public AjaxResult withdraw(@Valid @RequestBody WithdrawBody body)
    {
        String masterId = masterService.requireActiveMaster(SecurityUtils.getUserId());
        // 前端传元字符串，服务端统一转成分，避免浮点误差
        long amountFen;
        try
        {
            amountFen = new java.math.BigDecimal(body.amountYuan().trim())
                    .multiply(java.math.BigDecimal.valueOf(100))
                    .setScale(0, java.math.RoundingMode.HALF_UP).longValueExact();
        }
        catch (Exception exception)
        {
            throw new com.zjude.common.exception.ServiceException("提现金额格式不正确");
        }
        ZjudgMasterWithdraw withdraw = withdrawService.apply(masterId, amountFen, body.payAccount());
        return AjaxResult.success("提现申请已提交，请等待审核", withdraw);
    }

    /**
     * 我的提现记录。
     *
     * @return 提现列表
     */
    @GetMapping("/withdraws")
    public AjaxResult withdraws()
    {
        String masterId = masterService.requireActiveMaster(SecurityUtils.getUserId());
        return AjaxResult.success(withdrawService.listByMaster(masterId));
    }

    /**
     * 分转元字符串。
     *
     * @param fen 分
     * @return 元
     */
    private String yuan(long fen)
    {
        return java.math.BigDecimal.valueOf(fen)
                .divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * 流水金额转为元，便于前端直接展示。
     *
     * @param logs 原始流水
     * @return 展示用流水
     */
    private List<java.util.Map<String, Object>> toLogViews(List<ZjudgMasterWalletLog> logs)
    {
        return logs.stream().map(item -> {
            java.util.Map<String, Object> view = new java.util.LinkedHashMap<>();
            view.put("logId", item.getLogId());
            view.put("bizType", item.getBizType());
            view.put("amount", yuan(item.getAmount()));
            view.put("balanceAfter", yuan(item.getBalanceAfter()));
            view.put("bizNo", item.getBizNo());
            view.put("remark", item.getRemark());
            view.put("createTime", item.getCreateTime());
            return view;
        }).toList();
    }

    /** 提现请求体 */
    public record WithdrawBody(
            @NotBlank(message = "请填写提现金额")
            @jakarta.validation.constraints.Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "提现金额格式不正确")
            String amountYuan,
            @NotBlank(message = "请填写收款账号")
            @Size(max = 128, message = "收款账号过长") String payAccount)
    {
    }
}
