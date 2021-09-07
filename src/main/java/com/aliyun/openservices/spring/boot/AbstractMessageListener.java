package com.aliyun.openservices.spring.boot;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractMessageListener implements MessageListener {
	
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
