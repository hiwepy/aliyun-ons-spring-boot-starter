package com.aliyun.openservices.spring.boot;

import java.util.Objects;
import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import com.aliyun.openservices.ons.api.PropertyKeyConst;

import lombok.Data;

/**
 * ons配置参数
 * a参考：https://help.aliyun.com/document_detail/93574.html?spm=a2c4g.11186623.6.553.927d650eeh6vzK
 * <p>
 * AccessKey	                String	-	        您在阿里云账号管理控制台中创建的 AccessKeyId，用于身份认证。
 * SecretKey	                String	-	        您在阿里云账号管理控制台中创建的 AccessKeySecret，用于身份认证。
 * OnsChannel	                String	ALIYUN 用户渠道，阿里云：ALIYUN，聚石塔用户为：CLOUD。
 * NAMESRV_ADDR	                String	-	        设置 TCP 协议接入点。
 * GROUP_ID	                    String	-	        Consumer 实例的唯一 ID，您在控制台创建的 Group ID。
 * MessageModel	                String	CLUSTERING	设置 Consumer 实例的消费模式，集群消费：CLUSTERING，广播消费：BROADCASTING。
 * ConsumeThreadNums	        String	64	        消费线程数量。
 * MaxReconsumeTimes	        String	16	        设置消息消费失败的最大重试次数。
 * ConsumeTimeout	            String	15	        设置每条消息消费的最大超时时间，超过设置时间则被视为消费失败，等下次重新投递再次消费。每个业务需要设置一个合理的值，单位：分钟（min）。
 * ConsumeMessageBatchMaxSize	String	1	        BatchConsumer每次批量消费的最大消息数量，默认值为1，允许自定义范围为[1, 32]，实际消费数量可能小于该值。
 * CheckImmunityTimeInSeconds	String	30	        设置事务消息第一次回查的最快时间，单位：秒（s）。
 * suspendTimeMillis	        String	3000        只适用于顺序消息，设置消息消费失败的重试间隔时间，单位：毫秒（ms）。
 * </p>
 */
@ConfigurationProperties(prefix = AliyunOnsMqProperties.PREFIX)
@Data
public class AliyunOnsMqProperties {

	/**
     * The prefix of the property of {@link AliyunOnsMqProperties}.
     */
    public static final String PREFIX = "alibaba.cloud.ons";

	/**
	 * AccessKey, 用于标识、校验用户身份
	 */
	private String accessKey;
	/**
	 * SecretKey, 用于标识、校验用户身份
	 */
	private String secretKey;
	/**
	 * 使用STS时，需要配置STS Token, 详情参考https://help.aliyun.com/document_detail/28788.html
	 */
	private String securityToken;
	/**
	 * RAM角色授权的角色名称
	 */
	private String ramRoleName;
	/**
	 * Group ID，客户端ID
	 */
	private String groupId = "DEFAULT";
	/**
	 * 消费模式，包括集群模式、广播模式
	 */
	private String messageModel = "CLUSTERING";
	/**
	 * Name Server地址
	 */
	private String nameSrvAddr;
	/**
	 * 设置客户端接入来源，默认ALIYUN
	 */
	private String onsChannel = "ALIYUN";
	/**
	 * 是否启动vip channel
	 */
	private Boolean isVipChannelEnabled = Boolean.FALSE;

	/**
	 * 设置实例名，注意：如果在一个进程中将多个Producer或者是多个Consumer设置相同的InstanceName，底层会共享连接。
	 */
	private String instanceName;

	// -------producer------------------------------------------ 

    /**
     * Indicate whether add extend unique info for producer
     * eg:是否开启mqtransaction，用于使用exactly-once投递语义
     */
    private boolean addExtendUniqInfo = false;

    /**
     * If topic route not found when sending message, whether use the default topic route.
     */
    private boolean useDefaultTopicIfNotFound = true;

	/**
	 * 消息发送超时时间，如果服务端在配置的对应时间内未ACK，则发送客户端认为该消息发送失败。
	 */
	private long sendMsgTimeoutMillis = -1;
	
	// -------consumer------------------------------------------ 

	/**
	 * 订阅方是否是使用循环平均分配策略
	 */
	private String allocateMessageQueueStrategy;
	
	/**
	 * 消费线程数量
	 */
	private Integer consumeThreadNums;
    
    /**
     * Batch consumption size
     * BatchConsumer每次批量消费的最大消息数量, 默认值为1, 允许自定义范围为[1, 32], 实际消费数量可能小于该值.
     */
    private int consumeMessageBatchMaxSize = 1;

    /**
     * Max re-consume times. -1 means 16 times.
     * If messages are re-consumed more than {@link #maxReconsumeTimes} before success, it's be directed to a deletion
     * queue waiting.
     * a、消息消费失败时的最大重试次数。如果消息消费次数超过，还未成功，则将该消息转移到一个失败队列，等待被删除。
     */
    private int maxReconsumeTimes = -1;

    /**
     * Suspending pulling time for cases requiring slow pulling like flow-control scenario.
     * eg:顺序消息消费失败进行重试前的等待时间 单位(毫秒)
     */
    private long suspendTimeMillis = 1000;

    /**
     * Maximum amount of time in minutes a message may block the consuming thread.
     * eg:设置每条消息消费的最大超时时间,超过这个时间,这条消息将会被视为消费失败,等下次重新投递再次消费. 每个业务需要设置一个合理的值. 单位(分钟),默认15分钟
     */
    private long consumeTimeout = 15;

    /**
     * 	设置本地批量消费聚合时间. 默认是0, 即消息从服务端取到之后立即开始消费. 该时间最大为ConsumeTimeout的一半.
     */
    public long batchConsumeMaxAwaitDurationInSeconds = 0;
    
    /**
     * Whether enable accelerator of orderly consumption which consume messages concurrently with different sharding
     * keys in the same queue
     */
    private boolean orderlyConsumeAccelerator = false;

	/**
	 * Consumer允许在客户端中缓存的最大消息数量，默认值为5000，设置过大可能会引起客户端OOM，取值范围为[100, 50000]
	 * 考虑到批量拉取，实际最大缓存量会少量超过限定值
	 * 该限制在客户端级别生效，限定额会平均分配到订阅的Topic上，比如限制为1000条，订阅2个Topic，每个Topic将限制缓存500条
	 */
	private int maxCachedMessageAmount = 5000;

	/**
	 * Consumer允许在客户端中缓存的最大消息容量，默认值为512 MiB，设置过大可能会引起客户端OOM，取值范围为[16, 2048]
	 * 考虑到批量拉取，实际最大缓存量会少量超过限定值
	 * 该限制在客户端级别生效，限定额会平均分配到订阅的Topic上，比如限制为1000MiB，订阅2个Topic，每个Topic将限制缓存500MiB
	 */
	private int maxCachedMessageSizeInMiB = 512;

	/**
	 * 每次获取最大消息数量, 默认 1
	 */
	private Long maxBatchMessageCount = 1L;

	public Properties toProperties(AliyunProperties onsProperties) {
		
		Properties properties = new Properties();
		// AccessKey 阿里云身份验证，在阿里云服务器管理控制台创建
		String accessKey = StringUtils.hasText(this.getAccessKey()) ? this.getAccessKey() : onsProperties.getAccessKey();
		properties.put(PropertyKeyConst.AccessKey, accessKey);
		// SecretKey 阿里云身份验证，在阿里云服务器管理控制台创建
		String secretKey = StringUtils.hasText(this.getSecretKey()) ? this.getSecretKey() : onsProperties.getSecretKey();
		properties.put(PropertyKeyConst.SecretKey, secretKey);
		if(StringUtils.hasText(this.securityToken)) {
			properties.put(PropertyKeyConst.SecurityToken, this.securityToken);
		}
		if(StringUtils.hasText(this.ramRoleName)) {
			properties.put(PropertyKeyConst.RAM_ROLE_NAME, this.ramRoleName);
		}
		// 设置 TCP 接入域名（此处以公共云生产环境为例）
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
		// 设置发送超时时间，单位毫秒
		if (sendMsgTimeoutMillis > 0) {
			properties.put(PropertyKeyConst.SendMsgTimeoutMillis, this.sendMsgTimeoutMillis);
		}
		
		return properties;
	}
	
	public Properties toConsumerProperties(AliyunProperties onsProperties) {

		Properties properties = new Properties();
		// AccessKey 阿里云身份验证，在阿里云服务器管理控制台创建
		String accessKey = StringUtils.hasText(this.getAccessKey()) ? this.getAccessKey() : onsProperties.getAccessKey();
		properties.put(PropertyKeyConst.AccessKey, accessKey);
		// SecretKey 阿里云身份验证，在阿里云服务器管理控制台创建
		String secretKey = StringUtils.hasText(this.getSecretKey()) ? this.getSecretKey() : onsProperties.getSecretKey();
		properties.put(PropertyKeyConst.SecretKey, secretKey);
		if(StringUtils.hasText(this.securityToken)) {
			properties.put(PropertyKeyConst.SecurityToken, this.securityToken);
		}
		if(StringUtils.hasText(this.ramRoleName)) {
			properties.put(PropertyKeyConst.RAM_ROLE_NAME, this.ramRoleName);
		}
		// 设置 TCP 接入域名（此处以公共云生产环境为例）
		properties.put(PropertyKeyConst.NAMESRV_ADDR, this.nameSrvAddr);
		// 设置 默认分组
		properties.put(PropertyKeyConst.GROUP_ID, this.groupId);
		if(Objects.nonNull(this.instanceName)) {
			properties.put(PropertyKeyConst.InstanceName, this.instanceName);
		}
		// 消费线程数量
		if(Objects.nonNull(this.consumeThreadNums)) {
			properties.put(PropertyKeyConst.ConsumeThreadNums, this.consumeThreadNums);
		}
        properties.put(PropertyKeyConst.ConsumeMessageBatchMaxSize, this.consumeMessageBatchMaxSize);
		properties.put(PropertyKeyConst.MaxCachedMessageAmount, this.maxCachedMessageAmount);
		properties.put(PropertyKeyConst.MaxCachedMessageSizeInMiB, this.maxCachedMessageSizeInMiB);
		// 默认重试次数
        properties.put(PropertyKeyConst.MaxReconsumeTimes, this.maxReconsumeTimes);
        // 每次获取最大消息数量, 默认 1
        properties.put(PropertyKeyConst.MAX_BATCH_MESSAGE_COUNT, this.maxBatchMessageCount);
        properties.put(PropertyKeyConst.ENABLE_ORDERLY_CONSUME_ACCELERATOR, this.orderlyConsumeAccelerator);
        properties.put(PropertyKeyConst.ConsumeTimeout, this.consumeTimeout);
        properties.put(PropertyKeyConst.BatchConsumeMaxAwaitDurationInSeconds, this.batchConsumeMaxAwaitDurationInSeconds);
        properties.put(PropertyKeyConst.isVipChannelEnabled, this.isVipChannelEnabled);
		// 设置发送超时时间，单位毫秒
		if (sendMsgTimeoutMillis > 0) {
			properties.put(PropertyKeyConst.SendMsgTimeoutMillis, this.sendMsgTimeoutMillis);
		}
		
		return properties;
	}

}
