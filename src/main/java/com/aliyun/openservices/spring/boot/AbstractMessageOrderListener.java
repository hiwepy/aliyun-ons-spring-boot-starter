package com.aliyun.openservices.spring.boot;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.order.ConsumeOrderContext;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMessageOrderListener implements MessageOrderListener {

    private final Logger log = LoggerFactory.getLogger(AbstractMessageOrderListener.class);

    @Override
    public OrderAction consume(Message message, ConsumeOrderContext context) {
        log.info("MessageOrderListener start msgKey:{},topic:{},body：{}", message.getKey(), message.getTopic(), new String(message.getBody()));
        int count = countMqFlowByMqFlowId(message.getKey());
        if (count != 0) {
            log.warn("MessageOrderListener repeat consume  msgKey:{},topic:{},body：{}", message.getKey(), message.getTopic(), new String(message.getBody()));
            return OrderAction.Success;
        }
        try {
            orderConsume(message);
            saveMqFlow(message.getKey(), new String(message.getBody()));
            return OrderAction.Success;
        } catch (Exception e) {
            log.error("consume error topic:{},msgKey:{}", message.getTopic(), message.getKey(), e);
            //稍后重新消费
            return OrderAction.Suspend;
        }
    }


    /**
     * @param message :
     * @Author: 顺序消费
     * @Description:子类实现
     * @Date: 2020/7/15
     */
    public abstract void orderConsume(Message message) throws Exception;

    public abstract Integer countMqFlowByMqFlowId(String msgKey);

    public abstract void saveMqFlow(String msgKey, String msgBody);

}
