package com.aliyun.openservices.spring.boot;

import java.util.Properties;

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

@Configuration
@ConditionalOnClass({ ONSFactory.class })
@EnableConfigurationProperties(AliyunOnsMqProperties.class)
public class AliyunOnsAutoConfiguration {

	@Bean(destroyMethod = "shutdown")
	@ConditionalOnMissingBean
    public OrderProducer orderProducerBean(AliyunOnsMqProperties onsMqProperties) {
        Properties properties = onsMqProperties.toProperties();
        OrderProducerBean orderProducerBean = new OrderProducerBean();
        orderProducerBean.setProperties(properties);
        orderProducerBean.start();
        return orderProducerBean;
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public Producer producerBean(AliyunOnsMqProperties onsMqProperties) {
        Properties properties = onsMqProperties.toProperties();
        ProducerBean producerBean = new ProducerBean();
        producerBean.setProperties(properties);
        producerBean.start();
        return producerBean;
    }
    
	@Bean
	public AliyunOnsMqTemplate aliyunOnsMqTemplate() {
		return new AliyunOnsMqTemplate();
	}
	
}