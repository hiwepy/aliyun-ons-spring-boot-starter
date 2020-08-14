package com.aliyun.openservices.spring.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.order.ConsumeOrderContext;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderAction;

public abstract class AbstractMessageOrderListener implements MessageOrderListener {

    private final Logger log = LoggerFactory.getLogger(AbstractMessageOrderListener.class);
	
    @Override
    public OrderAction consume(Message message, ConsumeOrderContext context) {
        log.info("MessageOrderListener start msgKey:{},topic:{},body：{}", message.getKey(), message.getTopic(), new String(message.getBody()));
        int count = this.apply(message);
        if (count != 0) {
            log.warn("MessageOrderListener repeat consume  msgKey:{},topic:{},body：{}", message.getKey(), message.getTopic(), new String(message.getBody()));
            return OrderAction.Success;
        }
        try {
        	this.consume(count, message);
            return OrderAction.Success;
        } catch (Exception e) {
            log.error("consume error topic:{},msgKey:{}", message.getTopic(), message.getKey(), e);
            //稍后重新消费
            return OrderAction.Suspend;
        }
    }

    public abstract int apply(Message message);
    
    public abstract void consume(int count, Message message) throws Exception;
    

}
