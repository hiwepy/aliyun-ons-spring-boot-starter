package com.aliyun.openservices.spring.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;

public abstract class AbstractMessageListener implements MessageListener {
	
	private final Logger log = LoggerFactory.getLogger(AbstractMessageListener.class);

    @Override
    public Action consume(Message message, ConsumeContext context) {
        log.info("MessageListener start msgKey:{},topic:{},body：{}", message.getKey(), message.getTopic(), new String(message.getBody()));
        int count = this.apply(message);
        if (count != 0) {
            log.warn("MessageListener repeat consume  msgKey:{},topic:{},body：{}", message.getKey(), message.getTopic(), new String(message.getBody()));
            return Action.CommitMessage;
        }
        try {
            this.consume(count, message);
            return Action.CommitMessage;
        } catch (Exception e) {
            log.error("consume error topic:{},msgKey:{}", message.getTopic(), message.getKey(), e);
            //稍后重新消费
            return Action.ReconsumeLater;
        }
    }
    
    public abstract int apply(Message message);
    
    public abstract void consume(int count, Message message) throws Exception;
    
}
