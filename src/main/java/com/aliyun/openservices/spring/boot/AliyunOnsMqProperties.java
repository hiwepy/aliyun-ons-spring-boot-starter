package com.aliyun.openservices.spring.boot;

import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import com.aliyun.openservices.ons.api.PropertyKeyConst;

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
	 * Group ID，客户端ID
	 */
	private String groupId = "DEFAULT";
	/**
	 * 消息发送超时时间，如果服务端在配置的对应时间内未ACK，则发送客户端认为该消息发送失败。
	 */
	private long sendMsgTimeoutMillis = -1;
	/**
	 * 消费模式，包括集群模式、广播模式
	 */
	private String messageModel = "CLUSTERING";
	/**
	 * 消息队列服务接入点
	 */
	private String onsAddr;
	/**
	 * Name Server地址
	 */
	private String nameSrvAddr;
	/**
	 * 消费线程数量
	 */
	private Integer consumeThreadNums;
	/**
	 * 设置客户端接入来源，默认ALIYUN
	 */
	private String channel = "ALIYUN";
	/**
	 * 消息类型，可配置为NOTIFY、METAQ
	 */
	private String mqType;

	/**
	 * 是否启动vip channel
	 */
	private Boolean isVipChannelEnabled = Boolean.FALSE;

	/**
	 * 顺序消息消费失败进行重试前的等待时间 单位(毫秒)
	 */
	private long suspendTimeMillis = -1;

	/**
	 * 消息消费失败时的最大重试次数。如果消息消费次数超过，还未成功，则将该消息转移到一个失败队列，等待被删除。
	 */
	private int maxReconsumeTimes = 16;

	/**
	 * 设置每条消息消费的最大超时时间,超过这个时间,这条消息将会被视为消费失败,等下次重新投递再次消费. 每个业务需要设置一个合理的值.
	 * 单位(分钟),默认15分钟
	 */
	private int consumeTimeout = 15;
	/**
	 * 设置事务消息的第一次回查延迟时间
	 */
	private long checkImmunityTimeInSeconds;

	/**
	 * 是否每次请求都带上最新的订阅关系，默认false
	 */
	private Boolean postSubscriptionWhenPull = Boolean.FALSE;

	/**
	 * BatchConsumer每次批量消费的最大消息数量, 默认值为1, 允许自定义范围为[1, 32], 实际消费数量可能小于该值.
	 */
	private int consumeMessageBatchMaxSize = 1;

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
	 * 设置实例ID，充当命名空间的作用
	 */
	private String instanceId;

	/**
	 * 设置实例名，注意：如果在一个进程中将多个Producer或者是多个Consumer设置相同的InstanceName，底层会共享连接。
	 */
	private String instanceName = "InstanceName";

	/**
	 * MQ消息轨迹开关
	 */
	private Boolean msgTraceSwitch = Boolean.FALSE;
	/**
	 * Mqtt消息序列ID
	 */
	private String mqttMessageId;
	/**
	 * Mqtt消息
	 */
	private String mqttMessage;

	/**
	 * Mqtt消息保留关键字
	 */
	private String mqttPublishRetain = "mqttRetain";

	/**
	 * Mqtt消息保留关键字
	 */
	private String mqttPublishDubFlag = "mqttPublishDubFlag";

	/**
	 * Mqtt的二级Topic，是父Topic下的子类
	 */
	private String mqttSecondTopic = "mqttSecondTopic";

	/**
	 * Mqtt协议使用的每个客户端的唯一标识
	 */
	private String mqttClientId = "clientId";

	/**
	 * Mqtt消息传输的数据可靠性级别
	 */
	private String mqttQOS = "qoslevel";

	/**
	 * 是否开启mqtransaction，用于使用exactly-once投递语义
	 */
	private Boolean exactlyOnceDelivery = Boolean.FALSE;

	/**
	 * exactlyonceConsumer record manager 刷新过期记录周期
	 */
	private String exactlyOnceRmRefreshInterval = "exactlyOnceRmRefreshInterval";

	/**
	 * 每次获取最大消息数量
	 */
	private long maxBatchMessageCount = 1;

	public Properties toProperties(AliyunProperties onsProperties) {
		
		Properties properties = new Properties();
		// AccessKey 阿里云身份验证，在阿里云服务器管理控制台创建
		String accessKey = StringUtils.hasText(this.getAccessKey()) ? this.getAccessKey() : onsProperties.getAccessKey();
		properties.put(PropertyKeyConst.AccessKey, accessKey);
		// SecretKey 阿里云身份验证，在阿里云服务器管理控制台创建
		String secretKey = StringUtils.hasText(this.getSecretKey()) ? this.getSecretKey() : onsProperties.getSecretKey();
		properties.put(PropertyKeyConst.SecretKey, secretKey);
		// 设置 TCP 接入域名（此处以公共云生产环境为例）
		properties.put(PropertyKeyConst.NAMESRV_ADDR, this.nameSrvAddr);
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
		// 设置 TCP 接入域名（此处以公共云生产环境为例）
		properties.put(PropertyKeyConst.NAMESRV_ADDR, this.nameSrvAddr);
		// 设置 默认分组
		properties.put(PropertyKeyConst.GROUP_ID, this.groupId);
		// 默认重试次数
        properties.put(PropertyKeyConst.MaxReconsumeTimes, this.maxReconsumeTimes);
		// 设置发送超时时间，单位毫秒
		if (sendMsgTimeoutMillis > 0) {
			properties.put(PropertyKeyConst.SendMsgTimeoutMillis, this.sendMsgTimeoutMillis);
		}
		
		return properties;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getSecurityToken() {
		return securityToken;
	}

	public void setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public long getSendMsgTimeoutMillis() {
		return sendMsgTimeoutMillis;
	}

	public void setSendMsgTimeoutMillis(long sendMsgTimeoutMillis) {
		this.sendMsgTimeoutMillis = sendMsgTimeoutMillis;
	}

	public String getMessageModel() {
		return messageModel;
	}

	public void setMessageModel(String messageModel) {
		this.messageModel = messageModel;
	}

	public String getOnsAddr() {
		return onsAddr;
	}

	public void setOnsAddr(String onsAddr) {
		this.onsAddr = onsAddr;
	}

	public String getNameSrvAddr() {
		return nameSrvAddr;
	}

	public void setNameSrvAddr(String nameSrvAddr) {
		this.nameSrvAddr = nameSrvAddr;
	}

	public Integer getConsumeThreadNums() {
		return consumeThreadNums;
	}

	public void setConsumeThreadNums(Integer consumeThreadNums) {
		this.consumeThreadNums = consumeThreadNums;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getMqType() {
		return mqType;
	}

	public void setMqType(String mqType) {
		this.mqType = mqType;
	}

	public Boolean getIsVipChannelEnabled() {
		return isVipChannelEnabled;
	}

	public void setIsVipChannelEnabled(Boolean isVipChannelEnabled) {
		this.isVipChannelEnabled = isVipChannelEnabled;
	}

	public long getSuspendTimeMillis() {
		return suspendTimeMillis;
	}

	public void setSuspendTimeMillis(long suspendTimeMillis) {
		this.suspendTimeMillis = suspendTimeMillis;
	}

	public int getMaxReconsumeTimes() {
		return maxReconsumeTimes;
	}

	public void setMaxReconsumeTimes(int maxReconsumeTimes) {
		this.maxReconsumeTimes = maxReconsumeTimes;
	}

	public int getConsumeTimeout() {
		return consumeTimeout;
	}

	public void setConsumeTimeout(int consumeTimeout) {
		this.consumeTimeout = consumeTimeout;
	}

	public long getCheckImmunityTimeInSeconds() {
		return checkImmunityTimeInSeconds;
	}

	public void setCheckImmunityTimeInSeconds(long checkImmunityTimeInSeconds) {
		this.checkImmunityTimeInSeconds = checkImmunityTimeInSeconds;
	}

	public Boolean getPostSubscriptionWhenPull() {
		return postSubscriptionWhenPull;
	}

	public void setPostSubscriptionWhenPull(Boolean postSubscriptionWhenPull) {
		this.postSubscriptionWhenPull = postSubscriptionWhenPull;
	}

	public int getConsumeMessageBatchMaxSize() {
		return consumeMessageBatchMaxSize;
	}

	public void setConsumeMessageBatchMaxSize(int consumeMessageBatchMaxSize) {
		this.consumeMessageBatchMaxSize = consumeMessageBatchMaxSize;
	}

	public int getMaxCachedMessageAmount() {
		return maxCachedMessageAmount;
	}

	public void setMaxCachedMessageAmount(int maxCachedMessageAmount) {
		this.maxCachedMessageAmount = maxCachedMessageAmount;
	}

	public int getMaxCachedMessageSizeInMiB() {
		return maxCachedMessageSizeInMiB;
	}

	public void setMaxCachedMessageSizeInMiB(int maxCachedMessageSizeInMiB) {
		this.maxCachedMessageSizeInMiB = maxCachedMessageSizeInMiB;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public Boolean getMsgTraceSwitch() {
		return msgTraceSwitch;
	}

	public void setMsgTraceSwitch(Boolean msgTraceSwitch) {
		this.msgTraceSwitch = msgTraceSwitch;
	}

	public String getMqttMessageId() {
		return mqttMessageId;
	}

	public void setMqttMessageId(String mqttMessageId) {
		this.mqttMessageId = mqttMessageId;
	}

	public String getMqttMessage() {
		return mqttMessage;
	}

	public void setMqttMessage(String mqttMessage) {
		this.mqttMessage = mqttMessage;
	}

	public String getMqttPublishRetain() {
		return mqttPublishRetain;
	}

	public void setMqttPublishRetain(String mqttPublishRetain) {
		this.mqttPublishRetain = mqttPublishRetain;
	}

	public String getMqttPublishDubFlag() {
		return mqttPublishDubFlag;
	}

	public void setMqttPublishDubFlag(String mqttPublishDubFlag) {
		this.mqttPublishDubFlag = mqttPublishDubFlag;
	}

	public String getMqttSecondTopic() {
		return mqttSecondTopic;
	}

	public void setMqttSecondTopic(String mqttSecondTopic) {
		this.mqttSecondTopic = mqttSecondTopic;
	}

	public String getMqttClientId() {
		return mqttClientId;
	}

	public void setMqttClientId(String mqttClientId) {
		this.mqttClientId = mqttClientId;
	}

	public String getMqttQOS() {
		return mqttQOS;
	}

	public void setMqttQOS(String mqttQOS) {
		this.mqttQOS = mqttQOS;
	}

	public Boolean getExactlyOnceDelivery() {
		return exactlyOnceDelivery;
	}

	public void setExactlyOnceDelivery(Boolean exactlyOnceDelivery) {
		this.exactlyOnceDelivery = exactlyOnceDelivery;
	}

	public String getExactlyOnceRmRefreshInterval() {
		return exactlyOnceRmRefreshInterval;
	}

	public void setExactlyOnceRmRefreshInterval(String exactlyOnceRmRefreshInterval) {
		this.exactlyOnceRmRefreshInterval = exactlyOnceRmRefreshInterval;
	}

	public long getMaxBatchMessageCount() {
		return maxBatchMessageCount;
	}

	public void setMaxBatchMessageCount(long maxBatchMessageCount) {
		this.maxBatchMessageCount = maxBatchMessageCount;
	}

}
