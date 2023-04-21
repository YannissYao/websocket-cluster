package me.yannis.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import world.oasis.common.configuration.DefaultRocketMQAliConfiguration;
import world.oasis.common.configuration.RocketMqAliConfig;
import world.oasis.common.hashring.ConsistentHashRingConfig;

/**
 * @author yannis
 * @since 2021/3/19
 */
@EnableDiscoveryClient
@SpringBootApplication
@Import({RocketMqAliConfig.class, DefaultRocketMQAliConfiguration.class , ConsistentHashRingConfig.class})
public class GatewayApp {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApp.class, args);
    }
}
