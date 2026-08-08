package com.aliyun.openservices.spring.boot;

import java.util.List;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.batch.BatchMessageListener;

import lombok.extern.slf4j.Slf4j;

/**
 * Base implementation of a {@link BatchMessageListener} with idempotency support.
 * <p>Subclasses implement {@link #apply(Message)} (to record/track the message and return the
 * number of times it has already been consumed) and {@link #consume(int, Message, ConsumeContext)}
 * (the actual business logic). Messages already consumed are skipped.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractBatchMessageListener implements BatchMessageListener {

    /**
     * Optional subscription expression (tag filter) that overrides the annotation default.
     * @return the subscription expression, or {@code null} to use the default
     */
    public String expression(){
        return null;
    }

    @Override
    public Action consume(List<Message> messages, ConsumeContext context) {

    	for (Message message : messages) {
    		log.info("BatchMessageListener start msgKey:{},topic:{},body:{}", message.getKey(), message.getTopic(), new String(message.getBody()));
            int count = this.apply(message);
            if (count != 0) {
                log.warn("BatchMessageListener ignore repeat consume msgKey:{},topic:{},body:{}", message.getKey(), message.getTopic(), new String(message.getBody()));
                continue;
            }
            try {
            	this.consume(count, message, context);
            } catch (Exception e) {
                log.error("consume error topic:{},msgKey:{}", message.getTopic(), message.getKey(), e);
                return Action.ReconsumeLater; // Reconsume later
            }
		}

    	return Action.CommitMessage;
    }

    /**
     * Records the message and returns how many times it has been consumed so far.
     * @param message the incoming message
     * @return the consume count; non-zero means the message is a duplicate
     */
    public abstract int apply(Message message);

    /**
     * Consumes a single message.
     * @param count the consume count returned by {@link #apply(Message)}
     * @param message the message to consume
     * @param context the consume context
     * @throws Exception when consumption fails; the message will be reconsumed later
     */
    public abstract void consume(int count, Message message, ConsumeContext context) throws Exception;

}
