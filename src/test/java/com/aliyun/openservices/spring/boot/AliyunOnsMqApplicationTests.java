package com.aliyun.openservices.spring.boot;

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
