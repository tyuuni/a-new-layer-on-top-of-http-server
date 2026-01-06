package com.example.apiserver;

import brave.baggage.BaggageField;

public class ConfigConstants {
    public static final int ACCESS_END = 1;

    static final String SERVER_PORT = "server.port";

    static final String ENABLE_CONSOLE_LOGGING = "${app.log.console.enabled}";
    static final String LOG_LEVEL = "${app.log.level}";
    static final String LOG_INFO_FILE_PATH = "${app.log.info.path}";
    static final String LOG_INFO_FILE_ROLLING = "${app.log.info.rolling}";
    static final String LOG_ERROR_FILE_PATH = "${app.log.error.path}";
    static final String LOG_ERROR_FILE_ROLLING = "${app.log.error.rolling}";


    static final String ALIYUN_OSS_APP_ID = "${aliyun.oss.accessKeyId}";
    static final String ALIYUN_OSS_APP_KEY = "${aliyun.oss.accessKeySecret}";
    static final String ALIYUN_OSS_END_POINT = "${aliyun.oss.endPoint}";
    static final String ALIYUN_OSS_HOST = "${aliyun.oss.host}";
    static final String ALIYUN_OSS_BUCKET_NAME = "${aliyun.oss.bucket}";

    public static final BaggageField BRAVE_PROPAGATION_DEBUG_FIELD = BaggageField.create("eg-debug");

    static final String APP_NAME = "${spring.application.name}";

    public static final String LOGGING_TRACING_ID = "tid";
    public static final String LOGGING_UNIQUE_ID = "uid";
    public static final String LOGGING_PARENT_TRACING_ID = "pid";

    static final String MYSQL_CONFIG_MASTER_URL = "${mysql.url.master}";
    static final String MYSQL_CONFIG_SLAVE_URL = "${mysql.url.slave}";
    static final String MYSQL_CONFIG_USERNAME = "${mysql.username}";
    static final String MYSQL_CONFIG_PASSWORD = "${mysql.password}";

    private ConfigConstants() {
    }
}
