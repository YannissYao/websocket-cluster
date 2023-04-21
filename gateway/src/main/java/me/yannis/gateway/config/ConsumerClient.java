package me.yannis.gateway.config;

import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.ConsumerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import me.yannis.gateway.consumer.MqSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import world.oasis.base.constant.AppConfig;
import world.oasis.common.configuration.RocketMqAliConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


@Configuration
public class ConsumerClient {

    @Autowired
    private RocketMqAliConfig mqConfig;
    @Autowired
    private MqSubscriber mqSubscriber;


    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ConsumerBean buildConsumer() {
        ConsumerBean consumerBean = new ConsumerBean();
        //配置文件
        Properties properties = mqConfig.getMqPropertie();
        properties.setProperty(PropertyKeyConst.GROUP_ID, "GID_oasis-websocket");
        //将消费者线程数固定为20个 20为默认值
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, "50");
        consumerBean.setProperties(properties);


        Map<Subscription, MessageListener> subscriptionTable = new HashMap<Subscription, MessageListener>();
        Subscription subscription = new Subscription();
        subscription.setTopic(AppConfig.ROCKETMQ_OASIS_WEBSOCKET);
        subscription.setExpression("*");
        subscriptionTable.put(subscription, mqSubscriber);


        consumerBean.setSubscriptionTable(subscriptionTable);
        return consumerBean;
    }

}
