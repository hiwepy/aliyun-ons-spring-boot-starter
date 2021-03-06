package com.aliyun.openservices.spring.boot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.OnExceptionContext;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderProducer;
import com.aliyun.openservices.shade.com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.aliyun.openservices.spring.boot.annotation.MessageConsumer;
import com.aliyun.openservices.spring.boot.annotation.MessageOrderConsumer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AliyunOnsMqTemplate implements BeanFactoryPostProcessor {
 
	/*
	 * 上下文对象实例
	 */
	private ConfigurableListableBeanFactory applicationContext;
	
	private static ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("ons-pool-%d").build();

	private CompletionService<String> completionThreadPool;
	
	public AliyunOnsMqTemplate(AliyunOnsMqPoolProperties poolProperties) {

		/**
		 * corePoolSize    线程池核心池的大小
		 * maximumPoolSize 线程池中允许的最大线程数量
		 * keepAliveTime   当线程数大于核心时，此为终止前多余的空闲线程等待新任务的最长时间
		 * unit            keepAliveTime 的时间单位
		 * workQueue       用来储存等待执行任务的队列
		 * threadFactory   创建线程的工厂类
		 * handler         拒绝策略类,当线程池数量达到上线并且workQueue队列长度达到上限时就需要对到来的任务做拒绝处理
		 */
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
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		this.applicationContext = beanFactory;
		
	}

	/*
	 * 获取applicationContext
	 *
	 * @return
	 */
	public ConfigurableListableBeanFactory getApplicationContext() {
		return applicationContext;
	}
	
	/*
	 * 获取所有实现的消费者监听
	 * @return subscriptionTable
	 * @throws BeansException
	 */
	public Map<Subscription, MessageListener> getSubscriptionTable(String... arg) throws BeansException {
		try {
			String[] messageConsumerBeans = getApplicationContext().getBeanNamesForAnnotation(MessageConsumer.class);
			Map<Subscription, MessageListener> subscriptionTable = new HashMap<>(messageConsumerBeans.length);
			Subscription subscription;
			List<String> beanNames = Objects.isNull(arg) ? new ArrayList<String>() : Arrays.asList(arg);
			for (String beanName : messageConsumerBeans) {
				
				// 没有指定具体名称或在指定名称内
				if ( CollectionUtils.isEmpty(beanNames) || beanNames.contains(beanName)) {
					
					Class<?> clazz = applicationContext.getType(beanName);
					MessageConsumer messageConsumer = AnnotationUtils.findAnnotation(clazz, MessageConsumer.class);

					// 绑定监听的topic
					subscription = new Subscription();
					subscription.setTopic(messageConsumer.topic());
					// 绑定要监听的tag，多个tag用 || 隔开
					subscription.setExpression(messageConsumer.tag());

					subscriptionTable.put(subscription, (MessageListener) applicationContext.getBean(beanName));
					log.info("Topic[{}] and tag[{}] subscribed!", messageConsumer.topic(), messageConsumer.tag());

				}
				
			}
			log.info("Subscription Table : {}!", subscriptionTable);
			return subscriptionTable;
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return new HashMap<Subscription, MessageListener>();
	}

	/*
	 *  获取所有实现的顺序消费者监听
	 * @return {@link java.util.Map<com.aliyun.openservices.ons.api.bean.Subscription,com.aliyun.openservices.ons.api.order.MessageOrderListener>}
	 */
	public Map<Subscription, MessageOrderListener> getOrderSubscriptionTable(String... arg) throws BeansException {
		try {
			
			String[] messageConsumerBeans = getApplicationContext().getBeanNamesForAnnotation(MessageOrderConsumer.class);
			Map<Subscription, MessageOrderListener> subscriptionTable = new HashMap<>(messageConsumerBeans.length);
			Subscription subscription;
			List<String> beanNames = Objects.isNull(arg) ? new ArrayList<String>() : Arrays.asList(arg);
			for (String beanName : messageConsumerBeans) {
				
				// 没有指定具体名称或在指定名称内
				if (CollectionUtils.isEmpty(beanNames) || beanNames.contains(beanName)) {
					
					Class<?> clazz = applicationContext.getType(beanName);
					MessageOrderConsumer messageConsumer = AnnotationUtils.findAnnotation(clazz,
							MessageOrderConsumer.class);

					
					
					// 绑定监听的topic
					subscription = new Subscription();
					subscription.setTopic(messageConsumer.topic());
					// 绑定要监听的tag，多个tag用 || 隔开
					subscription.setExpression(messageConsumer.tag());

					subscriptionTable.put(subscription, (MessageOrderListener) applicationContext.getBean(beanName));
					log.info("Topic[{}] and tag[{}] subscribed!", messageConsumer.topic(), messageConsumer.tag());
					
				}

			}
			log.info("Subscription Table : {}!", subscriptionTable);
			return subscriptionTable;
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return new HashMap<Subscription, MessageOrderListener>();
	}
	
	/*
	 * 单条发送顺序消息
	 * 
	 * @param producer
	 * @param message     消息
	 * @param shardingKey 顺序消息选择因子
	 * @return
	 */
	public boolean sendOrderMes(OrderProducer producer, Message message, String shardingKey) {
		// 发信息必须给一个唯一标识key用于做幂等
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

	/*
	 * 同步发送消息
	 * 
	 * @param producer
	 * @param message
	 * @return
	 */
	public boolean sendSyncMes(Producer producer, Message message) {
		// 发信息必须给一个唯一标识key用于做幂等
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

	/*
	 * 异步发送消息
	 *
	 * @param producer
	 * @param msg
	 * @return
	 */
	public boolean sendSyncMessage(Producer producer, Message message) {
		try {
			producer.sendAsync(message, new SendCallback() {
				@Override
				public void onSuccess(final SendResult sendResult) {
					// 消费发送成功
					log.info(" Send mq message success. Topic is:" + sendResult.getTopic() + " msgId is: "
							+ sendResult.getMessageId());
				}

				@Override
				public void onException(OnExceptionContext context) {
					// 消息发送失败，需要进行重试处理，可重新发送这条消息或持久化这条数据进行补偿处理
					log.error(" Send mq message failed. Topic is:" + context.getTopic());
					;
				}
			});
			log.info("send message async. topic=" + message.getTopic() + ", msgId=" + message.getMsgID());
			return true;
		} catch (Exception e) {
			log.error(" Send mq message failed. Topic is: {}, msgId: {}, error : {}", message.getTopic(), message.getMsgID(), e.getMessage());
			return false;
		}
	}

	/*
	 * 异步发送消息
	 * 
	 * @param producer
	 * @param message
	 * @param sendCallback 回调
	 */
	public void sendAsyncMes(Producer producer, Message message, SendCallback sendCallback) {
		try {
			producer.sendAsync(message, sendCallback);
			// 在 callback 返回之前即可取得 msgId。
			log.info("send message async. topic=" + message.getTopic() + ", msgId=" + message.getMsgID());
		} catch (Exception e) {
			log.error(" Send mq message failed. Topic is: {}, msgId: {}, error : {}", message.getTopic(), message.getMsgID(), e.getMessage());
		}
	}

	public void sendAsyncMes(Producer producer, Message message) {
		producer.sendAsync(message, new SendCallback() {
			@Override
			public void onSuccess(SendResult sendResult) {
				// 在 callback 返回之前即可取得 msgId。
				log.info(
						"send message async successful. topic=" + message.getTopic() + ", msgId=" + message.getMsgID());
			}

			@Override
			public void onException(OnExceptionContext context) {
				log.error("send message async failed. topic=" + message.getTopic() + ", msgId=" + message.getMsgID());
			}
		});

	}

	/*
	 * 单向发送
	 * 
	 * @param producer
	 * @param message
	 * @return
	 */
	public boolean sendOneWayMes(Producer producer, Message message) {
		try {
			// 由于在 oneway
			// 方式发送消息时没有请求应答处理，一旦出现消息发送失败，则会因为没有重试而导致数据丢失。若数据不可丢，建议选用可靠同步或可靠异步发送方式。
			producer.sendOneway(message);
			log.info(" Send mq message success. Topic is:" + message.getTopic());
			return true;
		} catch (Exception e) {
			log.error(" Send mq message failed. Topic is: {}, msgId: {}, error : {}", message.getTopic(), message.getMsgID(), e.getMessage());
			return false;
		}
	}

	/*
	 * 多线程发送消息
	 * 
	 * @param producer
	 * @param message
	 */
	public void sendMultiMes(final Producer producer, final Message message) {
		completionThreadPool.submit(() -> {
			try {
				SendResult sendResult = producer.send(message);
				// 同步发送消息，只要不抛异常就是成功
				if (sendResult != null) {
					log.info(" Send mq message success. Topic is:" + message.getTopic() + " msgId is: "
							+ sendResult.getMessageId());
					
					return sendResult.getMessageId();
				}
			} catch (Exception e) {
				// 消息发送失败，需要进行重试处理，可重新发送这条消息或持久化这条数据进行补偿处理
				log.error(" Send mq message failed. Topic is: {}, msgId: {}, error : {}", message.getTopic(), message.getMsgID(), e.getMessage());
			}
			return "";
		});
	}

	/*
	 * 发送延时消息（延时执行）
	 * 
	 * @param producer
	 * @param message
	 * @param delayTime 延迟时间
	 * @return
	 */
	public boolean sendDelayMes(Producer producer, Message message, long delayTime) {
		// 发信息必须给一个唯一标识key用于做幂等
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

	/*
	 * 发送定时消息
	 * 
	 * @param producer
	 * @param message
	 * @param date
	 * @return
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
