package com.aliyun.openservices.spring.boot;

import java.util.Objects;
import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import com.aliyun.openservices.ons.api.PropertyKeyConst;

import lombok.Data;

/**
 * Configuration properties for Alibaba Cloud ONS (Message Queue for RocketMQ).
 * <p>Reference:
 * <a href="https://help.aliyun.com/document_detail/93574.html">TCP client parameters</a>.</p>
 * <ul>
 *   <li>AccessKey - AccessKeyId created in the Alibaba Cloud console, used for authentication.</li>
 *   <li>SecretKey - AccessKeySecret created in the Alibaba Cloud console, used for authentication.</li>
 *   <li>OnsChannel - User channel: {@code ALIYUN} for Alibaba Cloud, {@code CLOUD} for Jushita.</li>
 *   <li>NAMESRV_ADDR - TCP protocol access point.</li>
 *   <li>GROUP_ID - Unique id of the consumer instance (Group ID created in the console).</li>
 *   <li>MessageModel - Consume mode: {@code CLUSTERING} (cluster) or {@code BROADCASTING} (broadcast).</li>
 *   <li>ConsumeThreadNums - Number of consume threads (default 64).</li>
 *   <li>MaxReconsumeTimes - Maximum retry count on consume failure (default 16).</li>
 *   <li>ConsumeTimeout - Per-message consume timeout (minutes); on timeout the message is treated as failed and redelivered.</li>
 *   <li>ConsumeMessageBatchMaxSize - Maximum messages per batch consume (1-32, actual may be less).</li>
 *   <li>CheckImmunityTimeInSeconds - Earliest time (seconds) for the first transaction-message check.</li>
 *   <li>suspendTimeMillis - Retry interval for failed ordered-message consumption (milliseconds).</li>
 * </ul>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = AliyunOnsMqProperties.PREFIX)
@Data
public class AliyunOnsMqProperties {

	/**
     * The prefix of the property of {@link AliyunOnsMqProperties}.
     */
    public static final String PREFIX = "alibaba.cloud.ons";

	/** AccessKey id used to authenticate the caller. */
	private String accessKey;
	/** AccessKey secret used to authenticate the caller. */
	private String secretKey;
	/**
	 * STS token required when using Security Token Service.
	 * <p>See <a href="https://help.aliyun.com/document_detail/28788.html">STS documentation</a>.</p>
	 */
	private String securityToken;
	/** Name of the RAM role to assume. */
	private String ramRoleName;
	/** Group id (client id) of the consumer/producer. Defaults to {@code DEFAULT}. */
	private String groupId = "DEFAULT";
	/** Consume mode: {@code CLUSTERING} (cluster) or {@code BROADCASTING} (broadcast). */
	private String messageModel = "CLUSTERING";
	/** Name server address. */
	private String nameSrvAddr;
	/** Client access source. Defaults to {@code ALIYUN}. */
	private String onsChannel = "ALIYUN";
	/** Whether the VIP channel is enabled. */
	private Boolean isVipChannelEnabled = Boolean.FALSE;

	/**
	 * Instance name. When multiple producers or consumers share the same instance name they
	 * share the underlying connection within the process.
	 */
	private String instanceName;

	// -------producer------------------------------------------

    /**
     * Whether to add extended unique info for the producer.
     * When enabled it turns on mq-transaction support for exactly-once delivery semantics.
     */
    private boolean addExtendUniqInfo = false;

    /**
     * If topic route not found when sending message, whether use the default topic route.
     */
    private boolean useDefaultTopicIfNotFound = true;

	/**
	 * Send timeout in milliseconds. When the server does not ACK within this time the client
	 * treats the send as failed.
	 */
	private long sendMsgTimeoutMillis = -1;

	// -------consumer------------------------------------------

	/** Whether the subscriber uses the round-robin average allocation strategy. */
	private String allocateMessageQueueStrategy;

	/** Number of consume threads. */
	private Integer consumeThreadNums;

    /**
     * Batch consumption size: the maximum number of messages consumed per batch.
     * Defaults to 1; allowed range [1, 32] (actual amount may be smaller).
     */
    private int consumeMessageBatchMaxSize = 1;

    /**
     * Max re-consume times. -1 means 16 times.
     * If messages are re-consumed more than {@link #maxReconsumeTimes} before success, it's be directed to a deletion
     * queue waiting.
     */
    private int maxReconsumeTimes = -1;

    /**
     * Suspending pulling time for cases requiring slow pulling like flow-control scenario.
     * For ordered messages this is the wait time before a retry after a consume failure, in milliseconds.
     */
    private long suspendTimeMillis = 1000;

    /**
     * Maximum amount of time in minutes a message may block the consuming thread.
     * Beyond this time the message is treated as failed and redelivered. Defaults to 15 minutes.
     */
    private long consumeTimeout = 15;

    /**
     * Local batch-consume aggregation time in seconds. Defaults to 0, meaning messages are consumed
     * immediately after being fetched. The maximum is half of {@link #consumeTimeout}.
     */
    public long batchConsumeMaxAwaitDurationInSeconds = 0;

    /**
     * Whether enable accelerator of orderly consumption which consume messages concurrently with different sharding
     * keys in the same queue
     */
    private boolean orderlyConsumeAccelerator = false;

	/**
	 * Maximum number of messages the consumer may cache locally. Defaults to 5000; range [100, 50000].
	 * Due to batch pulling the actual amount may slightly exceed this limit. The limit applies at the
	 * client level and is evenly distributed across subscribed topics.
	 */
	private int maxCachedMessageAmount = 5000;

	/**
	 * Maximum cached message size in MiB. Defaults to 512; range [16, 2048].
	 * Due to batch pulling the actual amount may slightly exceed this limit. The limit applies at the
	 * client level and is evenly distributed across subscribed topics.
	 */
	private int maxCachedMessageSizeInMiB = 512;

	/** Maximum number of messages fetched per request. Defaults to 1. */
	private Long maxBatchMessageCount = 1L;

	/**
	 * Converts these properties (falling back to {@code onsProperties} for credentials) into the
	 * {@link Properties} map expected by the ONS producer/client.
	 * @param onsProperties the shared Alibaba Cloud account properties used as credential fallback
	 * @return the populated {@link Properties}
	 */
	public Properties toProperties(AliyunProperties onsProperties) {
		
		Properties properties = new Properties();
		// AccessKey for Alibaba Cloud authentication, created in the console.
		String accessKey = StringUtils.hasText(this.getAccessKey()) ? this.getAccessKey() : onsProperties.getAccessKey();
		properties.put(PropertyKeyConst.AccessKey, accessKey);
		// SecretKey for Alibaba Cloud authentication, created in the console.
		String secretKey = StringUtils.hasText(this.getSecretKey()) ? this.getSecretKey() : onsProperties.getSecretKey();
		properties.put(PropertyKeyConst.SecretKey, secretKey);
		if(StringUtils.hasText(this.securityToken)) {
			properties.put(PropertyKeyConst.SecurityToken, this.securityToken);
		}
		if(StringUtils.hasText(this.ramRoleName)) {
			properties.put(PropertyKeyConst.RAM_ROLE_NAME, this.ramRoleName);
		}
		// Set the TCP access domain (public cloud production environment shown as an example).
		properties.put(PropertyKeyConst.NAMESRV_ADDR, this.nameSrvAddr);
        properties.put(PropertyKeyConst.isVipChannelEnabled, this.isVipChannelEnabled);
        properties.put(PropertyKeyConst.OnsChannel, this.onsChannel);
        properties.put(PropertyKeyConst.EXACTLYONCE_DELIVERY, this.addExtendUniqInfo);

		if(Objects.nonNull(this.instanceName)) {
			properties.put(PropertyKeyConst.InstanceName, this.instanceName);
		}
		if (suspendTimeMillis > 0) {
			properties.put(PropertyKeyConst.SuspendTimeMillis, this.suspendTimeMillis);
		}
		// Set the send timeout, in milliseconds.
		if (sendMsgTimeoutMillis > 0) {
			properties.put(PropertyKeyConst.SendMsgTimeoutMillis, this.sendMsgTimeoutMillis);
		}

		return properties;
	}

	/**
	 * Converts these properties (falling back to {@code onsProperties} for credentials) into the
	 * {@link Properties} map expected by the ONS consumer client.
	 * @param onsProperties the shared Alibaba Cloud account properties used as credential fallback
	 * @return the populated consumer {@link Properties}
	 */
	public Properties toConsumerProperties(AliyunProperties onsProperties) {

		Properties properties = new Properties();
		// AccessKey for Alibaba Cloud authentication, created in the console.
		String accessKey = StringUtils.hasText(this.getAccessKey()) ? this.getAccessKey() : onsProperties.getAccessKey();
		properties.put(PropertyKeyConst.AccessKey, accessKey);
		// SecretKey for Alibaba Cloud authentication, created in the console.
		String secretKey = StringUtils.hasText(this.getSecretKey()) ? this.getSecretKey() : onsProperties.getSecretKey();
		properties.put(PropertyKeyConst.SecretKey, secretKey);
		if(StringUtils.hasText(this.securityToken)) {
			properties.put(PropertyKeyConst.SecurityToken, this.securityToken);
		}
		if(StringUtils.hasText(this.ramRoleName)) {
			properties.put(PropertyKeyConst.RAM_ROLE_NAME, this.ramRoleName);
		}
		// Set the TCP access domain (public cloud production environment shown as an example).
		properties.put(PropertyKeyConst.NAMESRV_ADDR, this.nameSrvAddr);
		// Set the default group id.
		properties.put(PropertyKeyConst.GROUP_ID, this.groupId);
		if(Objects.nonNull(this.instanceName)) {
			properties.put(PropertyKeyConst.InstanceName, this.instanceName);
		}
		// Number of consume threads.
		if(Objects.nonNull(this.consumeThreadNums)) {
			properties.put(PropertyKeyConst.ConsumeThreadNums, this.consumeThreadNums);
		}
        properties.put(PropertyKeyConst.ConsumeMessageBatchMaxSize, this.consumeMessageBatchMaxSize);
		properties.put(PropertyKeyConst.MaxCachedMessageAmount, this.maxCachedMessageAmount);
		properties.put(PropertyKeyConst.MaxCachedMessageSizeInMiB, this.maxCachedMessageSizeInMiB);
		// Default retry count.
        properties.put(PropertyKeyConst.MaxReconsumeTimes, this.maxReconsumeTimes);
        // Maximum number of messages fetched per request, default 1.
        properties.put(PropertyKeyConst.MAX_BATCH_MESSAGE_COUNT, this.maxBatchMessageCount);
        properties.put(PropertyKeyConst.ENABLE_ORDERLY_CONSUME_ACCELERATOR, this.orderlyConsumeAccelerator);
        properties.put(PropertyKeyConst.ConsumeTimeout, this.consumeTimeout);
        properties.put(PropertyKeyConst.BatchConsumeMaxAwaitDurationInSeconds, this.batchConsumeMaxAwaitDurationInSeconds);
        properties.put(PropertyKeyConst.isVipChannelEnabled, this.isVipChannelEnabled);
		// Set the send timeout, in milliseconds.
		if (sendMsgTimeoutMillis > 0) {
			properties.put(PropertyKeyConst.SendMsgTimeoutMillis, this.sendMsgTimeoutMillis);
		}

		return properties;
	}

}
