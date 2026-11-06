package com.zjude.schedule;

import java.time.Duration;
import com.zjude.schedule.service.IBatchJobTaskService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务调度入口。
 *
 * <p>所有任务都通过 {@link ZjudgScheduleLease} 加租约执行：
 * 应用为多实例部署，{@code @Scheduled} 会在每个实例触发，
 * 必须保证同一周期只有一个实例真正执行，否则自动派单会重复派单。</p>
 */
@Component
@Configurable
@EnableScheduling
public class ScheduleTask {

    private static final Logger log = LoggerFactory.getLogger(ScheduleTask.class);

    /** 批量作业锁名 */
    private static final String LOCK_BATCH_JOB = "zjudg:batch:job";

    /** 自动派单锁名 */
    private static final String LOCK_DISPATCH = "zjudg:dispatch:compensate";

    @Resource
    private IBatchJobTaskService batchJobTaskServer;

    @Resource
    private com.zjude.system.service.IZjudgDispatchService dispatchService;

    @Resource
    private ZjudgScheduleLease lease;

    // 每天十点三十分执行
    @Scheduled(cron = "0 30 10 * * ?")
    public void process(){
        // 租约 TTL 30 分钟：批量作业可能遍历较多记录，留足余量
        lease.runExclusively(LOCK_BATCH_JOB, Duration.ofMinutes(30), () -> {
            // 修改借阅记录状态（超过归还日期转为到期未还）
            log.info("定时任务修改借阅记录状态开始……");
            batchJobTaskServer.doTaskJob();
            log.info("定时任务修改借阅记录状态结束");
        });
    }

    /**
     * 自动派单补偿任务。
     *
     * <p>支付成功时的实时派单可能因瞬间无可用师傅而失败，
     * 这里每 5 分钟补偿一次，避免订单长期停留在待接单。</p>
     *
     * <p>派单开关关闭时方法内部直接返回，不会产生额外开销。</p>
     */
    @Scheduled(fixedDelay = 300_000L, initialDelay = 120_000L)
    public void dispatchPendingOrders()
    {
        // 租约 TTL 4 分钟：小于调度间隔 5 分钟，
        // 保证上一轮即使异常退出，下一轮也能正常接手而不会长期空转
        lease.runExclusively(LOCK_DISPATCH, Duration.ofMinutes(4), () -> {
            int success = dispatchService.dispatchPendingOrders();
            if (success > 0)
            {
                log.info("自动派单补偿完成，本次派单 {} 单", success);
            }
        });
    }
}
