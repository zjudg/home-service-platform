package com.zjude.schedule.service.impl;

import com.zjude.schedule.service.IBatchJobTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service("batchJobTaskServer")
public class BatchJobTaskServiceImp implements IBatchJobTaskService {

    private static final Logger log = LoggerFactory.getLogger(BatchJobTaskServiceImp.class);

    @Override
    public void doTaskJob() {
        log.info("Job start!");
    }
}
