package com.aliyun.openservices.spring.boot;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.bean.ConsumerBean;
import com.aliyun.openservices.ons.api.bean.OrderConsumerBean;
import com.aliyun.openservices.ons.api.bean.OrderProducerBean;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderProducer;

@Configuration
@ConditionalOnClass({ ONSFactory.class })
@EnableConfigurationProperties({ AliyunProperties.class, AliyunOnsMqProperties.class})
public class AliyunOnsAutoConfiguration {

	@Bean(destroyMethod = "shutdown")
	@ConditionalOnMissingBean
    public OrderProducer orderProducerBean( AliyunProperties onsProperties, AliyunOnsMqProperties onsMqProperties) {
        OrderProducerBean orderProducerBean = new OrderProducerBean();
        orderProducerBean.setProperties(onsMqProperties.toProperties(onsProperties));
        orderProducerBean.start();
        return orderProducerBean;
    }
	
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public Producer producerBean(AliyunProperties onsProperties, AliyunOnsMqProperties onsMqProperties) {
        ProducerBean producerBean = new ProducerBean();
        producerBean.setProperties(onsMqProperties.toProperties(onsProperties));
        producerBean.start();
        return producerBean;
    }
    
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public ConsumerBean consumerBean(AliyunProperties onsProperties, AliyunOnsMqProperties onsMqProperties, AliyunOnsMqTemplate aliyunOnsMqTemplate) {
        ConsumerBean consumerBean = new ConsumerBean();
        consumerBean.setProperties(onsMqProperties.toConsumerProperties(onsProperties));
        Map<Subscription, MessageListener> subscriptionTable = aliyunOnsMqTemplate.getSubscriptionTable();
        consumerBean.setSubscriptionTable(subscriptionTable);
        consumerBean.start();
        return consumerBean;
    }
    
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public OrderConsumerBean orderConsumerBean(AliyunProperties onsProperties, AliyunOnsMqProperties onsMqProperties, AliyunOnsMqTemplate aliyunOnsMqTemplate) {
        OrderConsumerBean consumerBean = new OrderConsumerBean();
        consumerBean.setProperties(onsMqProperties.toConsumerProperties(onsProperties));
        Map<Subscription, MessageOrderListener> subscriptionTable = aliyunOnsMqTemplate.getOrderSubscriptionTable();
        consumerBean.setSubscriptionTable(subscriptionTable);
        consumerBean.start();
        return consumerBean;
    }
    
	@Bean
	public AliyunOnsMqTemplate aliyunOnsMqTemplate() {
		return new AliyunOnsMqTemplate();
	}
	
}
