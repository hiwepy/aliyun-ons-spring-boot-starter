package com.aliyun.openservices.spring.boot;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;

import lombok.extern.slf4j.Slf4j;

/**
 * Base implementation of a {@link MessageListener} with idempotency support.
 * <p>Subclasses implement {@link #apply(Message)} (to record/track the message and return the
 * number of times it has already been consumed) and {@link #consume(int, Message)}
 * (the actual business logic). Messages already consumed are acknowledged immediately.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractMessageListener implements MessageListener {

    /**
     * Optional subscription expression (tag filter) that overrides the annotation default.
     * @return the subscription expression, or {@code null} to use the default
     */
    public String expression(){
        return null;
    }

    @Override
    /**
     * consume.
     *
     * @param message the message
     * @param context the context
     * @return the result
     */
    public Action consume(Message message, ConsumeContext context) {
        log.info("MessageListener start msgKey:{},topic:{},body:{}", message.getKey(), message.getTopic(), new String(message.getBody()));
        int count = this.apply(message);
        if (count != 0) {
            log.warn("MessageListener repeat consume  msgKey:{},topic:{},body:{}", message.getKey(), message.getTopic(), new String(message.getBody()));
            return Action.CommitMessage;
        }
        try {
            this.consume(count, message);
            return Action.CommitMessage;
        } catch (Exception e) {
            log.error("consume error topic:{},msgKey:{}", message.getTopic(), message.getKey(), e);
            // Reconsume later
            return Action.ReconsumeLater;
        }
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
     * @throws Exception when consumption fails; the message will be reconsumed later
     */
    public abstract void consume(int count, Message message) throws Exception;

}
