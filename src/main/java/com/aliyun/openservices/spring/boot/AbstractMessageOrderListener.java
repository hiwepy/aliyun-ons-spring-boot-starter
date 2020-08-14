package com.aliyun.openservices.spring.boot;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.order.ConsumeOrderContext;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderAction;

public abstract class AbstractMessageOrderListener implements MessageOrderListener {

    private final Logger log = LoggerFactory.getLogger(AbstractMessageOrderListener.class);
	private final Function<Message, Integer> function;
	private final BiConsumer<Integer, Message> consumer;
	
	public AbstractMessageOrderListener(Function<Message, Integer> function, BiConsumer<Integer, Message> consumer) {
		this.function = function;
		this.consumer = consumer;
	}
	
    @Override
    public OrderAction consume(Message message, ConsumeOrderContext context) {
        log.info("MessageOrderListener start msgKey:{},topic:{},body：{}", message.getKey(), message.getTopic(), new String(message.getBody()));
        int count = function.apply(message);
        if (count != 0) {
            log.warn("MessageOrderListener repeat consume  msgKey:{},topic:{},body：{}", message.getKey(), message.getTopic(), new String(message.getBody()));
            return OrderAction.Success;
        }
        try {
        	consumer.accept(count, message);
            return OrderAction.Success;
        } catch (Exception e) {
            log.error("consume error topic:{},msgKey:{}", message.getTopic(), message.getKey(), e);
            //稍后重新消费
            return OrderAction.Suspend;
        }
    }

}
