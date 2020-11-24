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
aliyun:
  ons:
    access-key: test
    secret-key: test
    name-srv-addr: http://zzzz.mq-internet-access.mq-internet.aliyuncs.com
    message-model: CLUSTERING    
    group-id: DEFAULT
```

##### 3、使用示例

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