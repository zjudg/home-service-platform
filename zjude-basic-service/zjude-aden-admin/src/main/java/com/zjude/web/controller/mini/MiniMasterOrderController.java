package com.zjude.web.controller.mini;

import java.util.List;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.page.TableDataInfo;
import com.zjude.common.utils.SecurityUtils;
import com.zjude.common.utils.StringUtils;
import com.zjude.system.domain.ZjudgOrder;
import com.zjude.system.service.IZjudgDispatchService;
import com.zjude.system.service.IZjudgMasterService;
import com.zjude.system.service.IZjudgOrderService;

/**
 * 小程序师傅端订单接口。
 *
 * <p>所有接口先经 {@link IZjudgMasterService#requireActiveMaster} 校验当前登录账号是
 * 「审核通过且未停用」的师傅，再以该师傅主键做数据过滤。仅依赖登录态（authenticated）
 * 不足以防越权，因为普通用户端账号同样处于已登录状态。</p>
 */
@RestController
@RequestMapping("/api/mini/master/orders")
public class MiniMasterOrderController extends BaseController
{
    @Resource
    private IZjudgOrderService orderService;

    @Resource
    private IZjudgMasterService masterService;

    @Resource
    private IZjudgDispatchService dispatchService;

    /**
     * 待接单池：未指派、待接单、预约日期不早于今天。
     *
     * @param slotId 可选时段过滤
     * @param onlyMatched 是否仅返回与本人技能匹配的订单（师傅自选）
     */
    @GetMapping("/pool")
    public TableDataInfo pool(@RequestParam(required = false) String slotId,
            @RequestParam(required = false, defaultValue = "false") boolean onlyMatched)
    {
        String masterId = masterService.requireActiveMaster(SecurityUtils.getUserId());
        startPage();
        List<ZjudgOrder> rows = orderService.selectPendingPool(slotId, masterId, onlyMatched);
        return getDataTable(rows);
    }

    /**
     * 我的订单，支持按状态过滤。
     */
    @GetMapping
    public TableDataInfo myOrders(@RequestParam(required = false) String status)
    {
        String masterId = masterService.requireActiveMaster(SecurityUtils.getUserId());
        startPage();
        List<ZjudgOrder> rows = orderService.selectByMaster(masterId, status);
        return getDataTable(rows);
    }

    /**
     * 我的订单详情，只允许查看本人承接的订单。
     */
    @GetMapping("/{orderId}")
    public AjaxResult detail(@PathVariable String orderId)
    {
        String masterId = masterService.requireActiveMaster(SecurityUtils.getUserId());
        ZjudgOrder order = orderService.selectForMaster(orderId, masterId);
        if (order == null)
        {
            // 待接单池中的订单在生产上同样不允许通过详情接口探测
            return AjaxResult.error("订单不存在");
        }
        return AjaxResult.success(order);
    }

    /**
     * 抢单。并发下只有一位师傅成功。
     */
    @PutMapping("/{orderId}/accept")
    public AjaxResult accept(@PathVariable String orderId)
    {
        String masterId = masterService.requireActiveMaster(SecurityUtils.getUserId());
        orderService.claimOrder(orderId, masterId);
        return AjaxResult.success("接单成功");
    }

    /**
     * 拒单。把系统派给自己、但自己不接的订单退回待派单池。
     *
     * <p>拒单会记入近期拒单次数，影响后续自动派单的排序权重；
     * 已开工的订单不允许拒单，需走后台异常处理。</p>
     */
    @PutMapping("/{orderId}/reject")
    public AjaxResult reject(@PathVariable String orderId, @RequestBody(required = false) RejectBody body)
    {
        String masterId = masterService.requireActiveMaster(SecurityUtils.getUserId());
        dispatchService.rejectDispatch(orderId, masterId, body == null ? null : body.reason());
        return AjaxResult.success("已拒单，订单将重新分配");
    }

    /**
     * 开工。
     */
    @PutMapping("/{orderId}/start")    public AjaxResult start(@PathVariable String orderId)
    {
        String masterId = masterService.requireActiveMaster(SecurityUtils.getUserId());
        orderService.startService(orderId, masterId);
        return AjaxResult.success("已开始服务");
    }

    /**
     * 完工，可附完工凭证图片。
     */
    @PutMapping("/{orderId}/finish")
    public AjaxResult finish(@PathVariable String orderId, @Valid @RequestBody(required = false) FinishBody body)
    {
        String masterId = masterService.requireActiveMaster(SecurityUtils.getUserId());
        String photos = body == null ? null : body.finishPhotos();
        orderService.finishService(orderId, masterId, photos);
        return AjaxResult.success("服务已完成");
    }

    /**
     * 待接单池是否需要展示「接单」按钮的判定，供前端复用同一份状态口径。
     *
     * @param row 订单
     * @return 可接单返回 true
     */
    static boolean isClaimable(ZjudgOrder row)
    {
        return row != null && StringUtils.isEmpty(row.getMasterId()) && "0".equals(row.getOrderStatus());
    }

    /** 完工请求体 */
    public record FinishBody(
            @Size(max = 1000, message = "完工凭证不能超过1000个字符") String finishPhotos)
    {
    }

    /** 拒单请求体，原因可不填 */
    public record RejectBody(
            @Size(max = 180, message = "拒单原因不能超过180个字符") String reason)
    {
    }
}
