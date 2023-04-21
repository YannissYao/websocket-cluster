package me.yannis.websocket;

import org.redisson.config.Config;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import world.oasis.common.configuration.DefaultRocketMQAliConfiguration;
import world.oasis.common.configuration.RocketMqAliConfig;
import world.oasis.common.hashring.ConsistentHashRingConfig;


@EnableDiscoveryClient
@SpringBootApplication
@EnableTransactionManagement
//@MapperScan(basePackages = {"world.oasis.game.mapper"})
@Import({RocketMqAliConfig.class, DefaultRocketMQAliConfiguration.class, ConsistentHashRingConfig.class})
@Configurable
//@NacosPropertySource(dataId = "${nacos.config.data-id}", autoRefreshed = true)
public class WebSocketApp {

    @Value("${spring.redis.host}")
    private String redisHost;
    @Value("${spring.redis.port}")
    private String redisPort;
    @Value("${spring.redis.password}")
    private String redisPwd;
//    @Value("${auth.name}")
//    private String name;
//    @Value("${auth.pwd}")
//    private String pwd;

    public static void main(String[] args) {
        SpringApplication.run(WebSocketApp.class);
    }


    @Primary
    @Bean(name = "redissonClient", destroyMethod = "shutdown")
    public RedissonClient redisson() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setPassword(redisPwd)
                .setDatabase(0)
                .setConnectionPoolSize(5)
                .setConnectionMinimumIdleSize(3)
                .setSubscriptionConnectionMinimumIdleSize(1)
                .setSubscriptionConnectionPoolSize(2)
                .setConnectTimeout(30000)
                .setSubscriptionsPerConnection(10000)
                .setTimeout(5000);

        return Redisson.create(config);
    }
}
