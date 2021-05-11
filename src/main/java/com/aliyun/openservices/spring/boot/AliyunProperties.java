package com.aliyun.openservices.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = AliyunProperties.PREFIX)
@Data
public class AliyunProperties {

	/**
     * The prefix of the property of {@link AliyunProperties}.
     */
    public static final String PREFIX = "alibaba.cloud";
    
	/**
	 * AccessKey, 用于标识、校验用户身份
	 */
	private String accessKey;
	/**
	 * SecretKey, 用于标识、校验用户身份
	 */
	private String secretKey;

}
