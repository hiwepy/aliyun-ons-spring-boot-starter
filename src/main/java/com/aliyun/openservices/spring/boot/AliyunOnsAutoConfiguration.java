package com.aliyun.openservices.spring.boot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.bean.OrderProducerBean;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.aliyun.openservices.ons.api.order.OrderProducer;

/**
 * Spring Boot auto-configuration for Alibaba Cloud ONS (Message Queue for RocketMQ).
 * <p>Registers the order producer, regular producer and the {@link AliyunOnsMqTemplate} helper.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass({ ONSFactory.class })
@EnableConfigurationProperties({ AliyunProperties.class, AliyunOnsMqProperties.class, AliyunOnsMqPoolProperties.class})
public class AliyunOnsAutoConfiguration {

	/**
	 * Creates and starts the ONS order (FIFO) producer.
	 * @param onsProperties the shared Alibaba Cloud account properties
	 * @param onsMqProperties the ONS-specific properties
	 * @return a started {@link OrderProducer}
	 */
	@Bean(destroyMethod = "shutdown")
	@ConditionalOnMissingBean
    public OrderProducer orderProducerBean( AliyunProperties onsProperties, AliyunOnsMqProperties onsMqProperties) {
        OrderProducerBean orderProducerBean = new OrderProducerBean();
        orderProducerBean.setProperties(onsMqProperties.toProperties(onsProperties));
        orderProducerBean.start();
        return orderProducerBean;
    }

	/**
	 * Creates and starts the ONS regular producer.
	 * @param onsProperties the shared Alibaba Cloud account properties
	 * @param onsMqProperties the ONS-specific properties
	 * @return a started {@link Producer}
	 */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public Producer producerBean(AliyunProperties onsProperties, AliyunOnsMqProperties onsMqProperties) {
        ProducerBean producerBean = new ProducerBean();
        producerBean.setProperties(onsMqProperties.toProperties(onsProperties));
        producerBean.start();
        return producerBean;
    }

	/**
	 * Creates the {@link AliyunOnsMqTemplate} helper bean.
	 * @param poolProperties the thread-pool properties used for asynchronous sending
	 * @return the ONS MQ template
	 */
	@Bean
	public AliyunOnsMqTemplate aliyunOnsMqTemplate(AliyunOnsMqPoolProperties poolProperties) {
		return new AliyunOnsMqTemplate(poolProperties);
	}

}
