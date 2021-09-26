# aliyun-ons-spring-boot-starter

#### 组件简介

 > 基于 ons-client 实现的 Spring Boot Starter 实现，依赖少，使用简单

#### 使用说明

##### 1、Spring Boot 项目添加 Maven 依赖

``` xml
<dependency>
	<groupId>com.github.hiwepy</groupId>
	<artifactId>aliyun-ons-spring-boot-starter</artifactId>
	<version>${project.version}</version>
</dependency>
```

##### 2、在`application.yml`文件中增加如下配置

```yaml
#################################################################################################
### 阿里云Ons配置：
#################################################################################################
alibaba:
  cloud:
    ons:
      access-key: test
      secret-key: test
      name-srv-addr: http://zzzz.mq-internet-access.mq-internet.aliyuncs.com
      message-model: CLUSTERING    
      group-id: DEFAULT
```

##### 3、使用示例

以一个支付订单检查为例，这里首先创建了消费者

```java

import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.ConsumerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.aliyun.openservices.spring.boot.AliyunOnsMqProperties;
import com.aliyun.openservices.spring.boot.AliyunOnsMqTemplate;
import com.aliyun.openservices.spring.boot.AliyunProperties;

@Configuration
@ConditionalOnClass({ONSFactory.class})
@EnableConfigurationProperties({AliyunProperties.class, AliyunOnsMqProperties.class})
public class OnsConsumerConfiguration {

    @Autowired
    private AliyunOnsMqTemplate aliyunOnsMqTemplate;

    /**
     * 1、支付检查消费者
     */
    @Bean(destroyMethod = "shutdown")
    public Consumer paymentCheckConsumer(AliyunProperties onsProperties, AliyunOnsMqProperties onsMqProperties) {
        Properties properties = onsMqProperties.toConsumerProperties(onsProperties);
        properties.put(PropertyKeyConst.GROUP_ID, "GID_paycheck");
        ConsumerBean consumerBean = new ConsumerBean();
        consumerBean.setProperties(properties);
        //将所有实现的消费者监听加入订阅关系
        Map<Subscription, MessageListener> subscriptionTable = aliyunOnsMqTemplate.getSubscriptionTable("paymentCheckListener");
        consumerBean.setSubscriptionTable(subscriptionTable);
        consumerBean.start();
        return consumerBean;
    }
    
}

```

然后是监听器实现！

```java

/**
 * 支付检查消息处理
 */
@Slf4j
@Component
@MessageConsumer(topic = "Pay_Check_Topic", tag = "paycheck")
public class PaymentCheckListener extends AbstractMessageListener {

    @Autowired
    private IProductOrderService productOrderService;

    @Override
    public void consume(int count, Message message) throws UnsupportedEncodingException {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("{} : {}", TopicConstant.PAY_CHECK_MESSAGE_TOPIC, body);

        // 1、消息内容序列化
        PreOrderCheckBO checkBo = JSONObject.parseObject(body, PreOrderCheckBO.class);

        // 2、调用接口检查订单
        getProductOrderService().checkOrder(checkBo);

    }

    public IProductOrderService getProductOrderService() {
		return productOrderService;
	}

}


```

下面是发消息

```java

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.order.OrderProducer;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AliyunOnsMqApplicationTests {

	@Autowired
	private AliyunOnsMqTemplate onsMqTemplate;
	@Autowired
	private OrderProducer orderProducer;
	@Autowired
	private Producer producer;
	
	
    @Test
    public void testPayCheckProducer() throws Exception {
    	
		// 1、构建订单检查参数Bo
	   PreOrderCheckBO checkBo = PreOrderCheckBO.builder()
			   .appId(orderBo.getAppId())
			   .appChannel(orderBo.getAppChannel())
			   .appVer(orderBo.getAppVer())
			   .feeType(orderVo.getFeeType())
			   .fromUid(orderBo.getFromUid())
			   .payChannel(orderBo.getPayChannel())
			   .productId(orderVo.getProductId())
			   .tradeNo(orderVo.getTradeNo())
			   .userId(orderBo.getUserId())
			   .build();

		// 2、支付检查消息
	    long delayTime = DateUtils.MILLIS_PER_MINUTE;
		Message messageMsg = new Message();
		messageMsg.setTopic("Pay_Check_Topic");
		messageMsg.setTag("paycheck");
		messageMsg.setKey(sequence.nextId().toString());
		messageMsg.setBody(JSONObject.toJSONString(checkBo).getBytes());
		aliyunOnsMqTemplate.sendDelayMes(producer, messageMsg, delayTime);
    }
	
	
    @Test
    public void testProducer() throws Exception {
    	Message message = new Message();
    	onsMqTemplate.sendAsyncMes(producer, message);
    }
    
    @Test
    public void testOrderProducer() throws Exception {
    	Message message = new Message();
    	onsMqTemplate.sendOrderMes(orderProducer, message, "");
    }

}

```

## Jeebiz 技术社区

Jeebiz 技术社区 **微信公共号**、**小程序**，欢迎关注反馈意见和一起交流，关注公众号回复「Jeebiz」拉你入群。

|公共号|小程序|
|---|---|
| ![](https://raw.githubusercontent.com/hiwepy/static/main/images/qrcode_for_gh_1d965ea2dfd1_344.jpg)| ![](https://raw.githubusercontent.com/hiwepy/static/main/images/gh_09d7d00da63e_344.jpg)|