package com.aliyun.openservices.spring.boot.annotation;


import java.lang.annotation.*;

/**
 * Marks a bean as a regular ONS message consumer.
 * <p>Applied to {@link com.aliyun.openservices.spring.boot.AbstractMessageListener} subclasses;
 * the topic and tag are used to build the subscription table.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface MessageConsumer {

    /** @return the topic to subscribe to */
    String topic();

    /** @return the tag filter; multiple tags are joined by {@code ||}. Defaults to {@code *} (all) */
    String tag() default "*";

}
