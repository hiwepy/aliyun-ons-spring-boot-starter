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

@Configuration
@ConditionalOnClass({ ONSFactory.class })
@EnableConfigurationProperties({ AliyunProperties.class, AliyunOnsMqProperties.class, AliyunOnsMqPoolProperties.class})
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
    
	@Bean
	public AliyunOnsMqTemplate aliyunOnsMqTemplate(AliyunOnsMqPoolProperties poolProperties) {
		return new AliyunOnsMqTemplate(poolProperties);
	}
	
}
