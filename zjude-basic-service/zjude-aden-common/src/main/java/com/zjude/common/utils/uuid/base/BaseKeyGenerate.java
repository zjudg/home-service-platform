package com.zjude.common.utils.uuid.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BaseKeyGenerate implements KeyGenerate {
    @Autowired
    private SnowflakeIdWorker idworker;

    public BaseKeyGenerate() {
    }

    public String generateStringKey(String type) {
        String tranDate = DateTool.formatNowDate("yyyyMMdd");
        return type + tranDate + this.generateLongKey();
    }

    public String snowflakeStringKey(String type) {
        String tranDate = DateTool.formatNowDate("yyyyMMdd");
        return type + tranDate + this.idworker.nextId();
    }

    public String snowflakeLongKey() {
        String tranDate = DateTool.formatNowDate("yyyyMMdd");
        return tranDate + this.idworker.nextId();
    }

    public String generateStringKey(String orgID, String code, String orgFlag) {
        return null;
    }

    public Long generateLongKey() {
        return System.currentTimeMillis() * 1000000L + (long)((int)(Math.random() * 900000.0));
    }

    public Boolean removeLongKey(String orgID, String type, String tranDate) {
        return null;
    }
}
