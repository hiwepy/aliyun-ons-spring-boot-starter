package com.aliyun.openservices.spring.boot;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.order.ConsumeOrderContext;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderAction;

import lombok.extern.slf4j.Slf4j;

/**
 * Base implementation of a {@link MessageOrderListener} with idempotency support.
 * <p>Subclasses implement {@link #apply(Message)} (to record/track the message and return the
 * number of times it has already been consumed) and {@link #consume(int, Message)}
 * (the actual business logic). Messages already consumed succeed immediately; a consumption
 * failure suspends (reconsuming) the ordered message.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractMessageOrderListener implements MessageOrderListener {

    /**
     * Optional subscription expression (tag filter) that overrides the annotation default.
     * @return the subscription expression, or {@code null} to use the default
     */
    public String expression(){
        return null;
    }

    @Override
    public OrderAction consume(Message message, ConsumeOrderContext context) {
        log.info("MessageOrderListener start msgKey:{},topic:{},body:{}", message.getKey(), message.getTopic(), new String(message.getBody()));
        int count = this.apply(message);
        if (count != 0) {
            log.warn("MessageOrderListener repeat consume  msgKey:{},topic:{},body:{}", message.getKey(), message.getTopic(), new String(message.getBody()));
            return OrderAction.Success;
        }
        try {
        	this.consume(count, message);
            return OrderAction.Success;
        } catch (Exception e) {
            log.error("consume error topic:{},msgKey:{}", message.getTopic(), message.getKey(), e);
            // Suspend (reconsume later)
            return OrderAction.Suspend;
        }
    }

    /**
     * Records the message and returns how many times it has been consumed so far.
     * @param message the incoming message
     * @return the consume count; non-zero means the message is a duplicate
     */
    public abstract int apply(Message message);

    /**
     * Consumes a single ordered message.
     * @param count the consume count returned by {@link #apply(Message)}
     * @param message the message to consume
     * @throws Exception when consumption fails; the message will be suspended and reconsumed
     */
    public abstract void consume(int count, Message message) throws Exception;


}
