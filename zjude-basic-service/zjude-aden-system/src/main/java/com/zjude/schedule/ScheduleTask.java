package com.zjude.schedule;

import com.zjude.schedule.service.IBatchJobTaskService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Configurable
@EnableScheduling
public class ScheduleTask {

    private static final Logger log = LoggerFactory.getLogger(ScheduleTask.class);

    @Resource
    private IBatchJobTaskService batchJobTaskServer;

    // 每天十点三十分执行
    @Scheduled(cron = "0 30 10 * * ?")
    private void process(){
        // 修改借阅记录状态（超过归还日期转为到期未还）
        log.info("定时任务修改借阅记录状态开始……");
        batchJobTaskServer.doTaskJob();
        log.info("定时任务修改借阅记录状态结�?.....");
    }
}
