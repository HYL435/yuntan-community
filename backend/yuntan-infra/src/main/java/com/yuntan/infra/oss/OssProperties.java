package com.yuntan.infra.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    /**
     * 是否启用 OSS
     */
    private Boolean enabled;

    /**
     * OSS 类型，例如 aliyun / minio
     */
    private String type;

    /**
     * 访问端点
     */
    private String endpoint;

    /**
     * AccessKey ID
     */
    private String accessKey;

    /**
     * AccessKey Secret
     */
    private String secretKey;

    /**
     * Bucket 名称
     */
    private String bucketName;

    /**
     * 访问域名
     */
    private String domain;

    /**
     * 上传目录前缀
     */
    private String uploadPrefix;
}