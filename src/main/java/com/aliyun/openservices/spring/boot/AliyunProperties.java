package com.aliyun.openservices.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Shared Alibaba Cloud account configuration properties used as credentials fallback.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = AliyunProperties.PREFIX)
@Data
public class AliyunProperties {

	/**
     * The prefix of the property of {@link AliyunProperties}.
     */
    public static final String PREFIX = "alibaba.cloud";

	/** AccessKey id used to identify and authenticate the user. */
	private String accessKey;
	/** AccessKey secret used to identify and authenticate the user. */
	private String secretKey;

}
