package com.aliyun.openservices.spring.boot;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;

public abstract class AbstractMessageListener implements MessageListener {
	
	private final Logger log = LoggerFactory.getLogger(AbstractMessageListener.class);
	private final Function<Message, Integer> function;
	private final BiConsumer<Integer, Message> consumer;
	
	public AbstractMessageListener(Function<Message, Integer> function, BiConsumer<Integer, Message> consumer) {
		this.function = function;
		this.consumer = consumer;
	}

    @Override
    public Action consume(Message message, ConsumeContext context) {
        log.info("MessageListener start msgKey:{},topic:{},body：{}", message.getKey(), message.getTopic(), new String(message.getBody()));
        int count = function.apply(message);
        if (count != 0) {
            log.warn("MessageListener repeat consume  msgKey:{},topic:{},body：{}", message.getKey(), message.getTopic(), new String(message.getBody()));
            return Action.CommitMessage;
        }
        try {
            consumer.accept(count, message);
            return Action.CommitMessage;
        } catch (Exception e) {
            log.error("consume error topic:{},msgKey:{}", message.getTopic(), message.getKey(), e);
            //稍后重新消费
            return Action.ReconsumeLater;
        }
    }
}
