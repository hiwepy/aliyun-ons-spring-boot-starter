package com.aliyun.openservices.spring.boot.annotation;


import java.lang.annotation.*;

/*
 * 消费者监听注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface BatchMessageConsumer {

    /*
     * topic
     *
     * @return
     */
    String topic();

    /*
     * subExpression
     *
     * @return
     */
    String subExpression() default "*";
   
}
