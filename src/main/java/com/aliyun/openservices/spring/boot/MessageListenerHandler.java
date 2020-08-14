package com.aliyun.openservices.spring.boot;

import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.spring.boot.annotation.MessageConsumer;
import com.aliyun.openservices.spring.boot.annotation.MessageOrderConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author daqian
 * @date 2019/3/20 20:13
 */
//@Component
//todo 用ApplicationContextAware时拿不到容器？？
//public class MessageListenerHandler implements ApplicationContextAware , BeanFactoryPostProcessor {
public class MessageListenerHandler implements BeanFactoryPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(MessageListenerHandler.class);

    /**
     * 上下文对象实例
     */
    private static ConfigurableListableBeanFactory applicationContext;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        applicationContext = beanFactory;
    }



    /**
     * 获取applicationContext
     *
     * @return
     */
    public static ConfigurableListableBeanFactory getApplicationContext() {
        return applicationContext;
    }

    /**
     * 获取所有实现的消费者监听
     *
     * @return subscriptionTable
     * @throws BeansException
     * @author daqian
     * @date 2019/3/21 15:46
     */
    public static Map<Subscription, MessageListener> getSubscriptionTable(String ...arg) throws BeansException {
        try {
            String[] messageConsumerBeans = getApplicationContext().getBeanNamesForAnnotation(MessageConsumer.class);
            Map<Subscription, MessageListener> subscriptionTable = new HashMap<>(messageConsumerBeans.length);
            Subscription subscription;
            for (String beanName : messageConsumerBeans) {
                if (!Arrays.asList(arg).contains(beanName)) {
                    continue;
                }
                Class clazz = applicationContext.getType(beanName);
                MessageConsumer messageConsumer = AnnotationUtils.findAnnotation(clazz, MessageConsumer.class);

                //绑定监听的topic
                subscription = new Subscription();
                subscription.setTopic(messageConsumer.topic());
                //绑定要监听的tag，多个tag用 || 隔开
                subscription.setExpression(messageConsumer.tag());

                subscriptionTable.put(subscription, (MessageListener) applicationContext.getBean(beanName));
                log.info("Topic[{}] and tag[{}] subscribed!", messageConsumer.topic(), messageConsumer.tag());

            }
            return subscriptionTable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @Author: cmm
     * @Description:获取所有实现的顺序消费者监听
     * @Date: 2020/7/11
     * @return {@link Map<com.aliyun.openservices.ons.api.bean.Subscription,com.aliyun.openservices.ons.api.order.MessageOrderListener>}
     */
    public static Map<Subscription, MessageOrderListener> getOrderSubscriptionTable(String ...arg) throws BeansException {
        try {
            String[] messageConsumerBeans = getApplicationContext().getBeanNamesForAnnotation(MessageOrderConsumer.class);
            Map<Subscription, MessageOrderListener> subscriptionTable = new HashMap<>(messageConsumerBeans.length);
            Subscription subscription;
            for (String beanName : messageConsumerBeans) {
                if (!Arrays.asList(arg).contains(beanName)) {
                    continue;
                }
                Class clazz = applicationContext.getType(beanName);
                MessageOrderConsumer messageConsumer = AnnotationUtils.findAnnotation(clazz, MessageOrderConsumer.class);

                //绑定监听的topic
                subscription = new Subscription();
                subscription.setTopic(messageConsumer.topic());
                //绑定要监听的tag，多个tag用 || 隔开
                subscription.setExpression(messageConsumer.tag());

                subscriptionTable.put(subscription, (MessageOrderListener) applicationContext.getBean(beanName));
                log.info("Topic[{}] and tag[{}] subscribed!", messageConsumer.topic(), messageConsumer.tag());

            }
            return subscriptionTable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
