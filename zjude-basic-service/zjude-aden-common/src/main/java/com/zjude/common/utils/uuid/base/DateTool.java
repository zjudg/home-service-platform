package com.zjude.common.utils.uuid.base;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;
import java.util.Map;


public class DateTool {
    private static final Map<String, DateTimeFormatter> sdfMap = new Hashtable<>();


    public DateTool() {
    }

    public static DateTimeFormatter getDateFormat(String format) {
        if (sdfMap.get(format) == null) {
            DateTimeFormatter sf = DateTimeFormatter.ofPattern(format);
            sdfMap.put(format, sf);
        }

        return sdfMap.get(format);
    }

    public static String formatNowDate(String format) {
        DateTimeFormatter dtf = getDateFormat(format);
        LocalDate date = LocalDate.now();
        return dtf.format(date);
    }

    static {
        sdfMap.put("yyyy-MM-dd HH:mm", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        sdfMap.put("yyyy-MM-dd HH:mm:ss", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        sdfMap.put("yyyy-MM-dd", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        sdfMap.put("yyyy/MM/dd HH:mm", DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
        sdfMap.put("yyyy/MM/dd HH:mm:ss", DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        sdfMap.put("yyyy/MM/dd", DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        sdfMap.put("yyyyMMdd", DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}