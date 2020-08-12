package com.aliyun.openservices.spring.boot.annotation;


import java.lang.annotation.*;

/**
 * 消费者监听注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface MessageOrderConsumer {

    /*
     * topic
     *
     * @return
     */
    String topic();

    /*
     * tag
     *
     * @return
     */
    String tag() default "*";

}
