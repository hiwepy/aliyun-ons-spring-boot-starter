package com.aliyun.openservices.spring.boot;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * ons 多线程发送配置参数
 * corePoolSize    线程池核心池的大小
 * maximumPoolSize 线程池中允许的最大线程数量
 * keepAliveTime   当线程数大于核心时，此为终止前多余的空闲线程等待新任务的最长时间
 * unit            keepAliveTime 的时间单位
 * </p>
 */
@ConfigurationProperties(prefix = AliyunOnsMqPoolProperties.PREFIX)
@Data
public class AliyunOnsMqPoolProperties {

	/**
     * The prefix of the property of {@link AliyunOnsMqPoolProperties}.
     */
    public static final String PREFIX = "alibaba.cloud.ons.pool";

	/**
	 * corePoolSize    线程池核心池的大小
	 */
	private Integer corePoolSize = Runtime.getRuntime().availableProcessors();
	/**
	 * maximumPoolSize 线程池中允许的最大线程数量
	 */
	private Integer maximumPoolSize = corePoolSize * 2;
	/**
	 * keepAliveTime   当线程数大于核心时，此为终止前多余的空闲线程等待新任务的最长时间
	 */
	private Long keepAliveTime = 0L;
	/**
	 * unit            keepAliveTime 的时间单位
	 */
	private TimeUnit unit = TimeUnit.MILLISECONDS;
	/**
	 * maximumWorkQueue 线程池中允许的最大等待执行任务数
	 */
	private Integer maximumWorkQueue = 1024;

}
