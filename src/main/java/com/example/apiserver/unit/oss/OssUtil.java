package com.example.apiserver.unit.oss;

import com.aliyun.oss.OSSClient;
import com.example.apiserver.core.BusinessUnit;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class OssUtil implements BusinessUnit {
    private final OSSClient ossClient;
    private final String ossBucket;
    private final String ossHost;

    public OssUtil(final OSSClient ossClient,
                   final String ossBucket,
                   final String ossHost) {
        this.ossClient = ossClient;
        this.ossBucket = ossBucket;
        this.ossHost = ossHost;
    }

    public String uploadToOss(final int subjectId,
                              final File file,
                              final String name,
                              final String suffix) {
        final var now = Instant.now();
        final var date = now.toString().substring(0, 7);
        final var ossPath = String.format(
                "keihin/admin-api/subject/%d/%s/%s.%s",
                subjectId,
                date,
                name,
                suffix);
        final var uploadResult = ossClient.putObject(
                ossBucket,
                ossPath,
                file);
        return String.format("%s/%s", ossHost, URLEncoder.encode(ossPath, StandardCharsets.UTF_8));
    }

}
