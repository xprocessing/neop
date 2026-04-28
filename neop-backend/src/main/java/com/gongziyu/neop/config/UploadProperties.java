package com.gongziyu.neop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置属性（文档33.4.2节）
 */
@Data
@Component
@ConfigurationProperties(prefix = "neop.upload")
public class UploadProperties {

    private String type;  // cos / oss / local

    // COS配置
    private String cosSecretId;
    private String cosSecretKey;
    private String cosRegion;
    private String cosBucket;
    private String cosDomain;

    // OSS配置
    private String ossEndpoint;
    private String ossAccessKeyId;
    private String ossAccessKeySecret;
    private String ossBucketName;
    private String ossDomain;

    // 本地配置
    private String localPath;
    private String localDomain;
}
