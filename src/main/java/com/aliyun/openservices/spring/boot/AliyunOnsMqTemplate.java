package com.aliyun.openservices.spring.boot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.OnExceptionContext;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderProducer;
import com.aliyun.openservices.spring.boot.annotation.MessageConsumer;
import com.aliyun.openservices.spring.boot.annotation.MessageOrderConsumer;

public class AliyunOnsMqTemplate implements BeanFactoryPostProcessor {

	private final Logger log = LoggerFactory.getLogger(AliyunOnsMqTemplate.class);
    private final String DELIMITER = "||";
	/*
	 * 上下文对象实例
	 */
	private ConfigurableListableBeanFactory applicationContext;

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		applicationContext = beanFactory;
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
	public Map<Subscription, MessageListener> getSubscriptionTable(String... beanNames) throws BeansException {
		
		try {
			
			// 1、查找上下文中被MessageOrderConsumer注解标注的对象名称
			String[] messageConsumerBeans = getApplicationContext().getBeanNamesForAnnotation(MessageConsumer.class);
			// 2、筛选出指定的对象上名称以及对象上的注解，并构建Map对象
			List<OnsConsumer> consumerList = new ArrayList<>();
			Stream.of(beanNames).filter(beanName -> {
				return Arrays.binarySearch(messageConsumerBeans, beanName) > -1;
			}).forEach(beanName -> {
				Class<?> clazz = applicationContext.getType(beanName);
				MessageConsumer messageConsumer = AnnotationUtils.findAnnotation(clazz,
						MessageConsumer.class);
				consumerList.add(OnsConsumer.builder()
						.beanName(beanName)
						.topic(messageConsumer.topic())
						.tag(messageConsumer.tag())
						.build());
			});
			// 3、按主题进行分组
			Map<String, List<OnsConsumer>> consumerTable = consumerList.stream().collect(Collectors.groupingBy(OnsConsumer::getTopic));
			// 4、按分组进行订阅
			Map<Subscription, MessageListener> subscriptionTable = new HashMap<>();
			for (Entry<String, List<OnsConsumer>> consumerEntry : consumerTable.entrySet()) {
				
				// 4.1、组织订阅集合
				consumerEntry.getValue().stream().map(item -> item.getBeanName()).distinct().forEach(beanName -> {
					
					// 4.2、绑定监听的topic
					Subscription subscription = new Subscription();
					subscription.setTopic(consumerEntry.getKey());
					// 4.3、绑定要监听的tag，多个tag用 || 隔开
					subscription.setExpression(consumerEntry.getValue().stream().map(item -> item.getTag()).distinct()
							.collect(Collectors.joining(DELIMITER)));
					
					subscriptionTable.put(subscription, (MessageListener) applicationContext.getBean(beanName));
					log.info("Topic[{}] and tag[{}] subscribed!", consumerEntry.getKey(), subscription.getExpression());
					
				});
			}
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
	public Map<Subscription, MessageOrderListener> getOrderSubscriptionTable(String... beanNames) throws BeansException {
		try {
			
			// 1、查找上下文中被MessageOrderConsumer注解标注的对象名称
			String[] messageConsumerBeans = getApplicationContext().getBeanNamesForAnnotation(MessageOrderConsumer.class);
			// 2、筛选出指定的对象上名称以及对象上的注解，并构建Map对象
			List<OnsConsumer> consumerList = new ArrayList<>();
			Stream.of(beanNames).filter(beanName -> {
				return Arrays.binarySearch(messageConsumerBeans, beanName) > -1;
			}).forEach(beanName -> {
				Class<?> clazz = applicationContext.getType(beanName);
				MessageOrderConsumer messageConsumer = AnnotationUtils.findAnnotation(clazz,
						MessageOrderConsumer.class);
				consumerList.add(OnsConsumer.builder()
						.beanName(beanName)
						.topic(messageConsumer.topic())
						.tag(messageConsumer.tag())
						.build());
			});
			// 3、按主题进行分组
			Map<String, List<OnsConsumer>> consumerTable = consumerList.stream().collect(Collectors.groupingBy(OnsConsumer::getTopic));
			// 4、按分组进行订阅
			Map<Subscription, MessageOrderListener> subscriptionTable = new HashMap<>();
			for (Entry<String, List<OnsConsumer>> consumerEntry : consumerTable.entrySet()) {
				
				// 4.1、组织订阅集合
				consumerEntry.getValue().stream().map(item -> item.getBeanName()).distinct().forEach(beanName -> {
					
					// 4.2、绑定监听的topic
					Subscription subscription = new Subscription();
					subscription.setTopic(consumerEntry.getKey());
					// 4.3、绑定要监听的tag，多个tag用 || 隔开
					subscription.setExpression(consumerEntry.getValue().stream().map(item -> item.getTag()).distinct()
							.collect(Collectors.joining(DELIMITER)));
					
					subscriptionTable.put(subscription, (MessageOrderListener) applicationContext.getBean(beanName));
					log.info("Topic[{}] and tag[{}] subscribed!", consumerEntry.getKey(), subscription.getExpression());
					
				});
			}
			
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
			e.printStackTrace();
			log.error(" Send mq message failed. Topic is:" + message.getTopic() + " msgId is: " + message.getMsgID());
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
			e.printStackTrace();
			log.error(" Send mq message failed. Topic is:" + message.getTopic() + " msgId is: " + message.getMsgID());
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
	public boolean sendSyncMessage(Producer producer, Message msg) {
		try {
			producer.sendAsync(msg, new SendCallback() {
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
			log.info("send message async. topic=" + msg.getTopic() + ", msgId=" + msg.getMsgID());
			return true;
		} catch (Exception e) {
			log.error(" Send mq message failed. Topic is:" + msg.getTopic() + " msgId is: " + msg.getMsgID());
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
		producer.sendAsync(message, sendCallback);
		// 在 callback 返回之前即可取得 msgId。
		log.info("send message async. topic=" + message.getTopic() + ", msgId=" + message.getMsgID());
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
			e.printStackTrace();
			log.error(" Send mq message failed. Topic is:" + message.getTopic() + " msgId is: " + message.getMsgID());
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
		Thread thread = new Thread(() -> {
			try {
				SendResult sendResult = producer.send(message);
				// 同步发送消息，只要不抛异常就是成功
				if (sendResult != null) {
					log.info(" Send mq message success. Topic is:" + message.getTopic() + " msgId is: "
							+ sendResult.getMessageId());
				}
			} catch (Exception e) {
				// 消息发送失败，需要进行重试处理，可重新发送这条消息或持久化这条数据进行补偿处理
				log.error(" Send mq message failed. Topic is:" + message.getTopic());
				e.printStackTrace();
			}
		});
		thread.start();
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
			long executeTime = new Date().getTime() + delayTime;
			message.setStartDeliverTime(executeTime);
			SendResult sendResult = producer.send(message);
			log.info(" Send mq message success. Topic is:" + message.getTopic() + " msgId is: "
					+ sendResult.getMessageId());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			log.error(" Send mq message failed. Topic is:" + message.getTopic() + " msgId is: " + message.getMsgID());
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
			e.printStackTrace();
			log.error(" Send mq message failed. Topic is:" + message.getTopic() + " msgId is: " + message.getMsgID());
			return false;
		}
	}
}
