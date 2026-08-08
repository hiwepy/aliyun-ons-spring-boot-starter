package com.aliyun.openservices.spring.boot.annotation;


import java.lang.annotation.*;

/**
 * Marks a bean as a batch ONS message consumer.
 * <p>Applied to {@link com.aliyun.openservices.spring.boot.AbstractBatchMessageListener} subclasses;
 * the topic and sub-expression are used to build the subscription table.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface BatchMessageConsumer {

    /** @return the topic to subscribe to */
    String topic();

    /** @return the subscription expression (tag filter); multiple tags joined by {@code ||}. Defaults to {@code *} (all) */
    String subExpression() default "*";

}
