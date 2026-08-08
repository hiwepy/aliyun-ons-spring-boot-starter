package com.aliyun.openservices.spring.boot;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Thread-pool properties used by {@link AliyunOnsMqTemplate} for multi-threaded message sending.
 * <ul>
 *   <li>{@code corePoolSize} - the core pool size</li>
 *   <li>{@code maximumPoolSize} - the maximum number of threads allowed in the pool</li>
 *   <li>{@code keepAliveTime} - the maximum time idle threads wait for new tasks before terminating</li>
 *   <li>{@code unit} - the time unit of {@code keepAliveTime}</li>
 * </ul>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = AliyunOnsMqPoolProperties.PREFIX)
@Data
public class AliyunOnsMqPoolProperties {

	/**
     * The prefix of the property of {@link AliyunOnsMqPoolProperties}.
     */
    public static final String PREFIX = "alibaba.cloud.ons.pool";

	/** Core pool size. Defaults to the number of available processors. */
	private Integer corePoolSize = Runtime.getRuntime().availableProcessors();
	/** Maximum number of threads allowed in the pool. Defaults to twice the core size. */
	private Integer maximumPoolSize = corePoolSize * 2;
	/** Maximum time idle threads wait for new tasks before terminating. */
	private Long keepAliveTime = 0L;
	/** Time unit of {@link #keepAliveTime}. */
	private TimeUnit unit = TimeUnit.MILLISECONDS;
	/** Maximum number of tasks queued before they are executed. */
	private Integer maximumWorkQueue = 1024;

}
