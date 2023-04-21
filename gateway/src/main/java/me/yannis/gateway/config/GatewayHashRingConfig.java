package me.yannis.gateway.config;

import lombok.extern.slf4j.Slf4j;
import me.yannis.gateway.filter.CustomReactiveLoadBalanceFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import world.oasis.common.hashring.ConsistentHashRouter;
import world.oasis.common.hashring.ServiceNode;


/**
 * 初始化
 *
 * @author yannis
 * @since 2021/3/23
 */
@Slf4j
@Configuration
public class GatewayHashRingConfig {

    @Autowired
    private RedissonClient redissonClient;
    final RedisTemplate<Object, Object> redisTemplate;
    final WebSocketProperties webSocketProperties;
    private final LoadBalancerClientFactory clientFactory;

    public GatewayHashRingConfig(RedisTemplate<Object, Object> redisTemplate,
                                 WebSocketProperties webSocketProperties,
                                 LoadBalancerClientFactory clientFactory) {
        this.redisTemplate = redisTemplate;
        this.webSocketProperties = webSocketProperties;
        this.clientFactory = clientFactory;
    }

    /**
     * @param client                 负载均衡客户端
     * @param loadBalancerProperties 负载均衡配置
     * @param consistentHashRouter   {@link #init() init方法}注入，此处未使用构造注入（会产生循环依赖）
     * @param discoveryClient        服务发现客户端
     * @return 注入自定义的 Reactive 过滤器 Bean 对象
     */
    @Bean
    public CustomReactiveLoadBalanceFilter customReactiveLoadBalanceFilter(LoadBalancerClient client,
                                                                           LoadBalancerProperties loadBalancerProperties,
                                                                           ConsistentHashRouter<ServiceNode> consistentHashRouter,
                                                                           DiscoveryClient discoveryClient) {
        log.debug("初始化 自定义响应式负载均衡器: {}, {}", client, loadBalancerProperties);
        return new CustomReactiveLoadBalanceFilter(clientFactory, loadBalancerProperties, consistentHashRouter, discoveryClient, webSocketProperties);
    }

    //    @Bean
//    @SuppressWarnings("unchecked")
//    public ConsistentHashRouter<ServiceNode> init() {
//        // 先从 Redis 中获取哈希环(网关集群)
//        final Map<Object, Object> ring = redisTemplate.opsForHash().entries(GlobalConstant.HASH_RING_REDIS);
//        // 获取环中的所有真实节点
//        Set<ServiceNode> serviceNodes = Sets.newHashSet();
//        for (Object key : ring.keySet()) {
//            Long hashKey = (Long) key;
//            VirtualNode<ServiceNode> virtualNode = (VirtualNode<ServiceNode>) ring.get(hashKey);
//            ServiceNode physicalNode = virtualNode.getPhysicalNode();
//            serviceNodes.add(physicalNode);
//        }
//        ConsistentHashRouter<ServiceNode> consistentHashRouter = new ConsistentHashRouter<>(serviceNodes, GlobalConstant.VIRTUAL_COUNT);
//        log.debug("初始化 ConsistentHashRouter: {}", consistentHashRouter);
//        return consistentHashRouter;
//    }


    @Bean
    public ConsistentHashRouter<ServiceNode> init() {
        ConsistentHashRouter consistentHashRouter = new ConsistentHashRouter<ServiceNode>(redissonClient);
        consistentHashRouter.init();
        return consistentHashRouter;
    }
}
