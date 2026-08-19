package com.aliyun.openservices.spring.boot;

import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.ons.api.batch.BatchMessageListener;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderProducer;
import com.aliyun.openservices.ons.shaded.com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.aliyun.openservices.ons.shaded.commons.lang3.exception.ExceptionUtils;
import com.aliyun.openservices.spring.boot.annotation.BatchMessageConsumer;
import com.aliyun.openservices.spring.boot.annotation.MessageConsumer;
import com.aliyun.openservices.spring.boot.annotation.MessageOrderConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.*;

/**
 * Helper template for sending Alibaba Cloud ONS messages and discovering annotated consumers.
 * <p>Acts as a {@link BeanFactoryPostProcessor} to capture the bean factory, then exposes
 * convenience methods for synchronous, asynchronous, one-way, ordered, delayed and timed
 * message sending. It also scans for beans annotated with
 * {@link MessageConsumer}/{@link BatchMessageConsumer}/{@link MessageOrderConsumer} to build
 * the subscription tables.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@Slf4j
public class AliyunOnsMqTemplate implements BeanFactoryPostProcessor {

	/** The captured bean factory (application context). */
	private ConfigurableListableBeanFactory applicationContext;

	private static ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("ons-pool-%d").build();

	/** Completion-based thread pool used for multi-threaded sending. */
	private CompletionService<String> completionThreadPool;

	/** Default callback used for asynchronous sends without an explicit callback. */
	private static SendCallback SEND_CALLBACK = new SendCallback() {

		@Override
		/**
		 * on Success.
		 *
		 * @param sendResult the send result
		 */
		public void onSuccess(SendResult sendResult) {
			// The msgId is available before the callback returns.
			log.info("send message async successful. topic={}, msgId={}", sendResult.getTopic() , sendResult.getMessageId());
		}

		@Override
		/**
		 * on Exception.
		 *
		 * @param context the context
		 */
		public void onException(OnExceptionContext context) {
			log.error("send message async failed. topic={},msgId={}, error: {}" , context.getTopic(), context.getMessageId(), ExceptionUtils.getMessage(context.getException()));
		}
	};

	/**
	 * Creates the template and initialises its internal thread pool.
	 * <p>Pool parameters (with their meaning):
	 * <ul>
	 *   <li>{@code corePoolSize} - core pool size</li>
	 *   <li>{@code maximumPoolSize} - maximum number of threads allowed in the pool</li>
	 *   <li>{@code keepAliveTime} - maximum time idle threads wait for new tasks before terminating</li>
	 *   <li>{@code unit} - time unit of {@code keepAliveTime}</li>
	 *   <li>{@code workQueue} - queue holding tasks waiting to execute</li>
	 *   <li>{@code threadFactory} - factory used to create new threads</li>
	 *   <li>{@code handler} - rejection policy applied when the pool is full and the queue is saturated</li>
	 * </ul>
	 * @param poolProperties the thread-pool configuration
	 */
	public AliyunOnsMqTemplate(AliyunOnsMqPoolProperties poolProperties) {

		ExecutorService threadPool = new ThreadPoolExecutor(
				poolProperties.getCorePoolSize(),
				poolProperties.getMaximumPoolSize(),
				poolProperties.getKeepAliveTime(),
				poolProperties.getUnit(),
		        new LinkedBlockingQueue<>(poolProperties.getMaximumWorkQueue()),
		        namedThreadFactory,
		        new ThreadPoolExecutor.AbortPolicy()
		);
		this.completionThreadPool = new ExecutorCompletionService<>(threadPool);
	}

	@Override
	/**
	 * post Process Bean Factory.
	 *
	 * @param beanFactory the bean factory
	 * @throws BeansException if an error occurs
	 */
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		this.applicationContext = beanFactory;

	}

	/**
	 * Returns the captured bean factory.
	 * @return the application context
	 */
	public ConfigurableListableBeanFactory getApplicationContext() {
		return applicationContext;
	}

	/**
	 * Builds the subscription table for all {@link MessageConsumer}-annotated beans.
	 * @param arg optional bean-name filter; when empty all consumer beans are included
	 * @return the subscription table
	 * @throws BeansException when bean resolution fails
	 */
	public Map<Subscription, MessageListener> getSubscriptionTable(String... arg) throws BeansException {
		try {
			String[] messageConsumerBeans = getApplicationContext().getBeanNamesForAnnotation(MessageConsumer.class);
			Map<Subscription, MessageListener> subscriptionTable = new HashMap<>(messageConsumerBeans.length);
			Subscription subscription;
			List<String> beanNames = Objects.isNull(arg) ? new ArrayList<String>() : Arrays.asList(arg);
			for (String beanName : messageConsumerBeans) {

				// Include when no filter is given or the bean name matches the filter.
				if ( CollectionUtils.isEmpty(beanNames) || beanNames.contains(beanName)) {

					Class<?> clazz = applicationContext.getType(beanName);
					MessageConsumer messageConsumer = AnnotationUtils.findAnnotation(clazz, MessageConsumer.class);

					// Bind the topic to listen on.
					subscription = new Subscription();
					subscription.setTopic(messageConsumer.topic());
					// Bind the tag filter; multiple tags are joined by ||.
					subscription.setExpression(messageConsumer.tag());

					// Extension hook.
					Object messageListener = applicationContext.getBean(beanName);
					if(messageListener instanceof AbstractMessageListener){
						AbstractMessageListener aMessageListener = (AbstractMessageListener) messageListener;
						String expression = aMessageListener.expression();
						if(StringUtils.hasText(expression)){
							subscription.setExpression(expression);
						}
					}

					subscriptionTable.put(subscription, (MessageListener) messageListener);
					log.info("Topic[{}] and tag[{}] subscribed!", messageConsumer.topic(), messageConsumer.tag());

				}

			}
			log.info("Subscription Table : {}!", subscriptionTable);
			return subscriptionTable;
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return new HashMap<>(0);
	}

	/**
	 * Builds the subscription table for all {@link BatchMessageConsumer}-annotated beans.
	 * @param arg optional bean-name filter; when empty all consumer beans are included
	 * @return the subscription table
	 * @throws BeansException when bean resolution fails
	 */
	public Map<Subscription, BatchMessageListener> getBatchSubscriptionTable(String... arg) throws BeansException {
		try {
			String[] messageConsumerBeans = getApplicationContext().getBeanNamesForAnnotation(BatchMessageConsumer.class);
			Map<Subscription, BatchMessageListener> subscriptionTable = new HashMap<>(messageConsumerBeans.length);
			Subscription subscription;
			List<String> beanNames = Objects.isNull(arg) ? new ArrayList<String>() : Arrays.asList(arg);
			for (String beanName : messageConsumerBeans) {

				// Include when no filter is given or the bean name matches the filter.
				if ( CollectionUtils.isEmpty(beanNames) || beanNames.contains(beanName)) {

					Class<?> clazz = applicationContext.getType(beanName);
					BatchMessageConsumer messageConsumer = AnnotationUtils.findAnnotation(clazz, BatchMessageConsumer.class);

					// Bind the topic to listen on.
					subscription = new Subscription();
					subscription.setTopic(messageConsumer.topic());
					// Bind the tag filter; multiple tags are joined by ||.
					subscription.setExpression(messageConsumer.subExpression());
					// Extension hook.
					Object messageListener = applicationContext.getBean(beanName);
					if(messageListener instanceof AbstractBatchMessageListener){
						AbstractBatchMessageListener batchMessageListener = (AbstractBatchMessageListener) messageListener;
						String expression = batchMessageListener.expression();
						if(StringUtils.hasText(expression)){
							subscription.setExpression(expression);
						}
					}
					subscriptionTable.put(subscription, (BatchMessageListener) messageListener);
					log.info("Topic[{}] and subExpression[{}] subscribed!", messageConsumer.topic(), messageConsumer.subExpression());

				}

			}
			log.info("Subscription Table : {}!", subscriptionTable);
			return subscriptionTable;
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return new HashMap<>();
	}

	/**
	 * Builds the subscription table for all {@link MessageOrderConsumer}-annotated beans.
	 * @param arg optional bean-name filter; when empty all consumer beans are included
	 * @return the ordered subscription table
	 * @throws BeansException when bean resolution fails
	 */
	public Map<Subscription, MessageOrderListener> getOrderSubscriptionTable(String... arg) throws BeansException {
		try {

			String[] messageConsumerBeans = getApplicationContext().getBeanNamesForAnnotation(MessageOrderConsumer.class);
			Map<Subscription, MessageOrderListener> subscriptionTable = new HashMap<>(messageConsumerBeans.length);
			Subscription subscription;
			List<String> beanNames = Objects.isNull(arg) ? new ArrayList<String>() : Arrays.asList(arg);
			for (String beanName : messageConsumerBeans) {

				// Include when no filter is given or the bean name matches the filter.
				if (CollectionUtils.isEmpty(beanNames) || beanNames.contains(beanName)) {

					Class<?> clazz = applicationContext.getType(beanName);
					MessageOrderConsumer messageConsumer = AnnotationUtils.findAnnotation(clazz, MessageOrderConsumer.class);

					// Bind the topic to listen on.
					subscription = new Subscription();
					subscription.setTopic(messageConsumer.topic());
					// Bind the tag filter; multiple tags are joined by ||.
					subscription.setExpression(messageConsumer.tag());

					// Extension hook.
					Object messageListener = applicationContext.getBean(beanName);
					if(messageListener instanceof AbstractMessageOrderListener){
						AbstractMessageOrderListener messageOrderListener = (AbstractMessageOrderListener) messageListener;
						String expression = messageOrderListener.expression();
						if(StringUtils.hasText(expression)){
							subscription.setExpression(expression);
						}
					}

					subscriptionTable.put(subscription, (MessageOrderListener) messageListener);
					log.info("Topic[{}] and tag[{}] subscribed!", messageConsumer.topic(), messageConsumer.tag());

				}

			}
			log.info("Subscription Table : {}!", subscriptionTable);
			return subscriptionTable;
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return new HashMap<>(0);
	}

	/**
	 * Sends a single ordered message.
	 * @param producer the order producer
	 * @param message the message to send
	 * @param shardingKey the sharding key used to select the ordered queue
	 * @return {@code true} when the message is sent successfully
	 */
	public boolean sendOrderMes(OrderProducer producer, Message message, String shardingKey) {
		// A unique key is required for idempotency.
		Assert.hasText(message.getKey(), "message key must not be empty ");
		try {
			SendResult sendResult = producer.send(message, shardingKey);
			log.info(" Send mq message success. Topic is:" + message.getTopic() + " msgId is: "
					+ sendResult.getMessageId());
			return true;
		} catch (Exception e) {
			log.error(" Send mq message failed. Topic is: {}, msgId: {}, error : {}", message.getTopic(), message.getMsgID(), e.getMessage());
			return false;
		}
	}

	/**
	 * Sends a message synchronously.
	 * @param producer the producer
	 * @param message the message to send
	 * @return {@code true} when the message is sent successfully
	 */
	public boolean sendSyncMes(Producer producer, Message message) {
		// A unique key is required for idempotency.
		Assert.hasText(message.getKey(), "message key must not be empty ");
		try {
			SendResult sendResult = producer.send(message);
			log.info(" Send mq message success. Topic is:" + message.getTopic() + " msgId is: "
					+ sendResult.getMessageId());
			return true;
		} catch (Exception e) {
			log.error(" Send mq message failed. Topic is: {}, msgId: {}, error : {}", message.getTopic(), message.getMsgID(), e.getMessage());
			return false;
		}
	}

	/**
	 * Sends a message asynchronously using the default callback.
	 * @param producer the producer
	 * @param message the message to send
	 * @return {@code true} when the send is accepted
	 */
	public boolean sendAsyncMes(Producer producer, Message message) {
		try {
			producer.sendAsync(message, SEND_CALLBACK);
			log.info("send message async. topic=" + message.getTopic() + ", msgId=" + message.getMsgID());
			return true;
		} catch (Exception e) {
			log.error(" Send mq message failed. Topic is: {}, msgId: {}, error : {}", message.getTopic(), message.getMsgID(), e.getMessage());
			return false;
		}
	}

	/**
	 * Sends a message asynchronously with a custom callback.
	 * @param producer the producer
	 * @param message the message to send
	 * @param sendCallback the callback invoked on success or failure
	 */
	public void sendAsyncMes(Producer producer, Message message, SendCallback sendCallback) {
		try {
			producer.sendAsync(message, sendCallback);
			// The msgId is available before the callback returns.
			log.info("send message async. topic=" + message.getTopic() + ", msgId=" + message.getMsgID());
		} catch (Exception e) {
			log.error(" Send mq message failed. Topic is: {}, msgId: {}, error : {}", message.getTopic(), message.getMsgID(), e.getMessage());
		}
	}

	/**
	 * Sends a message one-way (no acknowledgement).
	 * <p>Because one-way sending has no request/response, a send failure is not retried and the
	 * message may be lost. For data that must not be lost use the reliable sync or async methods.</p>
	 * @param producer the producer
	 * @param message the message to send
	 * @return {@code true} when the message is accepted by the producer
	 */
	public boolean sendOneWayMes(Producer producer, Message message) {
		try {
			producer.sendOneway(message);
			log.info(" Send mq message success. Topic is:" + message.getTopic());
			return true;
		} catch (Exception e) {
			log.error(" Send mq message failed. Topic is: {}, msgId: {}, error : {}", message.getTopic(), message.getMsgID(), e.getMessage());
			return false;
		}
	}

	/**
	 * Sends a message using the internal thread pool.
	 * @param producer the producer
	 * @param message the message to send
	 */
	public void sendMultiMes(final Producer producer, final Message message) {
		completionThreadPool.submit(() -> {
			try {
				SendResult sendResult = producer.send(message);
				// A synchronous send is successful when no exception is thrown.
				if (sendResult != null) {
					log.info(" Send mq message success. Topic is:" + message.getTopic() + " msgId is: "
							+ sendResult.getMessageId());

					return sendResult.getMessageId();
				}
			} catch (Exception e) {
				// On send failure you should retry or persist the message for compensation.
				log.error(" Send mq message failed. Topic is: {}, msgId: {}, error : {}", message.getTopic(), message.getMsgID(), e.getMessage());
			}
			return "";
		});
	}

	/**
	 * Sends a delayed message that will be delivered after {@code delayTime} milliseconds.
	 * @param producer the producer
	 * @param message the message to send
	 * @param delayTime the delay before delivery, in milliseconds
	 * @return {@code true} when the message is sent successfully
	 */
	public boolean sendDelayMes(Producer producer, Message message, long delayTime) {
		// A unique key is required for idempotency.
		Assert.hasText(message.getKey(), "message key must not be empty ");
		try {
			long executeTime = System.currentTimeMillis() + delayTime;
			message.setStartDeliverTime(executeTime);
			SendResult sendResult = producer.send(message);
			log.info(" Send mq message success. Topic is:" + message.getTopic() + " msgId is: "
					+ sendResult.getMessageId());
			return true;
		} catch (Exception e) {
			log.error(" Send mq message failed. Topic is: {}, msgId: {}, error : {}", message.getTopic(), message.getMsgID(), e.getMessage());
			return false;
		}
	}

	/**
	 * Sends a timed message that will be delivered at the given date/time.
	 * @param producer the producer
	 * @param message the message to send
	 * @param date the scheduled delivery time
	 * @return {@code true} when the message is sent successfully
	 */
	public boolean sendTimingMes(Producer producer, Message message, Date date) {
		try {
			long executeTime = date.getTime();
			message.setStartDeliverTime(executeTime);
			SendResult sendResult = producer.send(message);
			log.info(" Send mq message success. Topic is:" + message.getTopic() + " msgId is: "
					+ sendResult.getMessageId());
			return true;
		} catch (Exception e) {
			log.error(" Send mq message failed. Topic is: {}, msgId: {}, error : {}", message.getTopic(), message.getMsgID(), e.getMessage());
			return false;
		}
	}
}
