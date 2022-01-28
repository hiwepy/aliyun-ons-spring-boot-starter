package com.aliyun.openservices.spring.boot;

import java.util.List;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.batch.BatchMessageListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractBatchMessageListener implements BatchMessageListener {

    public String expression(){
        return null;
    }

    @Override
    public Action consume(List<Message> messages, ConsumeContext context) {

    	for (Message message : messages) {
    		log.info("BatchMessageListener start msgKey:{},topic:{},body：{}", message.getKey(), message.getTopic(), new String(message.getBody()));
            int count = this.apply(message);
            if (count != 0) {
                log.warn("BatchMessageListener ignore repeat consume msgKey:{},topic:{},body：{}", message.getKey(), message.getTopic(), new String(message.getBody()));
                continue;
            }
            try {
            	this.consume(count, message, context);
            } catch (Exception e) {
                log.error("consume error topic:{},msgKey:{}", message.getTopic(), message.getKey(), e);
                return Action.ReconsumeLater; // 稍后重新消费
            }
		}

    	return Action.CommitMessage;
    }

    public abstract int apply(Message message);

    public abstract void consume(int count, Message message, ConsumeContext context) throws Exception;

}
