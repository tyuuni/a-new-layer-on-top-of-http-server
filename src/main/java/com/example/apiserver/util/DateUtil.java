package com.example.apiserver.util;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateUtil {
    public final static ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");
    public final static DateTimeFormatter DB_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm")
            .withZone(CHINA_ZONE);
    public final static DateTimeFormatter MM_DD_YYYY = DateTimeFormatter
            .ofPattern("MM/dd/yyyy HH:mm")
            .withZone(CHINA_ZONE);
    public final static DateTimeFormatter YYYY_MM_DD = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(CHINA_ZONE);
    public final static DateTimeFormatter YYYYMMDD = DateTimeFormatter
            .ofPattern("yyyy/MM/dd")
            .withZone(CHINA_ZONE);
    public final static DateTimeFormatter YYYY_MM_DD_HH_MM_SS = DateTimeFormatter
            .ofPattern("yyyy/MM/dd HH:mm:ss")
            .withZone(CHINA_ZONE);
    public final static DateTimeFormatter YYYYMMDDHHMMSSSSS = DateTimeFormatter
            .ofPattern("yyyyMMddHHmmssSSS")
            .withZone(CHINA_ZONE);
    public final static DateTimeFormatter YYYY_MM_ = DateTimeFormatter
            .ofPattern("yyyy年MM月")
            .withZone(CHINA_ZONE);

    private DateUtil() {
    }
}
