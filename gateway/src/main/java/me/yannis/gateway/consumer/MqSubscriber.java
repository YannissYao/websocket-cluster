package me.yannis.gateway.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.vip.vjtools.vjkit.mapper.JsonMapper;
import lombok.SneakyThrows;
import me.yannis.gateway.config.WebSocketProperties;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import world.oasis.common.constant.GlobalConstant;
import world.oasis.common.hashring.ConsistentHashRouter;
import world.oasis.common.hashring.ServiceNode;
import world.oasis.common.hashring.VirtualNode;
import world.oasis.common.model.WebSocketMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用 mq pub/sub 订阅消息
 *
 * @author yannis
 * @since 2021/3/23
 */
@Component
public class MqSubscriber implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(MqSubscriber.class);

    final DiscoveryClient discoveryClient; // spring cloud native interface
    final WebSocketProperties webSocketProperties;
    final RedisTemplate<Object, Object> redisTemplate;
    //    final FanoutSender fanoutSender;
    private ConsistentHashRouter<ServiceNode> consistentHashRouter;
    @Autowired
    private RedissonClient redissonClient;

    public MqSubscriber(DiscoveryClient discoveryClient,
                        WebSocketProperties webSocketProperties,
                        ConsistentHashRouter<ServiceNode> consistentHashRouter,
                        RedisTemplate<Object, Object> redisTemplate) {
        this.discoveryClient = discoveryClient;
        this.webSocketProperties = webSocketProperties;
        this.consistentHashRouter = consistentHashRouter;
        this.redisTemplate = redisTemplate;
//        this.fanoutSender = fanoutSender;
    }

    public void setConsistentHashRouter(ConsistentHashRouter<ServiceNode> consistentHashRouter) {
        this.consistentHashRouter = consistentHashRouter;
    }


    @SneakyThrows
    @Override
    public Action consume(Message message, ConsumeContext consumeContext) {
        String tag = message.getTag();

        WebSocketMessage webSocketMessage = null;
        try {
            webSocketMessage = JsonMapper.INSTANCE.fromJson(new String(message.getBody(), "UTF-8"), WebSocketMessage.class);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        logger.info("【Redis 订阅】网关收到 WebSocket 实例变化消息: {}", webSocketMessage);
        String upOrDown = webSocketMessage.getContent();
        // 实例的标识：IP
        String serverIp = webSocketMessage.getServerIp();
        logger.info("【哈希环】该实例上线之前为 {}, 稍后将更新...", JsonMapper.INSTANCE.toJson(consistentHashRouter.getRing())); // NOSONAR
        if (GlobalConstant.SERVER_UP_MESSAGE.equalsIgnoreCase(upOrDown)) {
            // 实例上线, 但 Nacos 可能尚未发现服务，此处再等 Nacos 获取到最新服务列表
            Thread.sleep(3000);
            // 一个服务上线了，应当告知原本哈希到其他节点但现在路由到此节点的所有客户端断开连接
            // 为了确定是哪些客户端需要重连，可以遍历所有 userId 和哈希，筛选出节点添加前后匹配到不同真实节点的所有 userId
            // 因此，每次 WebSocket 有新连接(onOpen)的时候都有必要将 userId(+hash) 保存在 redis 中，然后在节点变动时取出
            Map<Object, Object> userIdAndHashInRedis = redisTemplate.opsForHash().entries(GlobalConstant.KEY_TO_BE_HASHED);
            logger.debug("Redis 中 userId hash : {}", userIdAndHashInRedis);
            Map<String, ServiceNode> oldUserAndServer = new ConcurrentHashMap<>();
            for (Object userIdObj : userIdAndHashInRedis.keySet()) {
                String userId = (String) userIdObj;
                Long oldHashObj = Long.valueOf(userIdAndHashInRedis.get(userId).toString());
                ServiceNode oldServiceNode = consistentHashRouter.routeNode(userId);
                logger.debug("【遍历】当前客户端 [{}] 的旧节点 [{}]", userId, oldServiceNode);
                // 如果 WebSocket 实例上线之前就有了客户端的连接，重连间隙可能只有几秒，极有可能此时哈希环是空的
                // https://github.com/Lonor/websocket-cluster/issues/2
                if (null != oldServiceNode) {
                    oldUserAndServer.put(userId, oldServiceNode);
                }
            }
            // 向 Hash 环添加 node
            ServiceNode serviceNode = new ServiceNode(serverIp);
            consistentHashRouter.addNode(serviceNode, GlobalConstant.VIRTUAL_COUNT);
            // 添加了 node 之后就可能有部分 userId 路由到的真实服务节点发生变动
            List<String> userIdClientsToReset = new ArrayList<>();
            for (Map.Entry<String, ServiceNode> entry : oldUserAndServer.entrySet()) {
                ServiceNode newServiceNode = consistentHashRouter.routeNode(entry.getKey());
                logger.debug("【遍历】当前客户端 [{}] 的新节点 [{}]", entry.getKey(), newServiceNode);
                // 同一 userId 路由到的真实服务节点前后可能会不一样, 把这些 userId 筛选出来
                if (!newServiceNode.getKey().equals(entry.getValue().getKey())) {
                    userIdClientsToReset.add(entry.getKey());
                    logger.info("【哈希环更新】客户端在哈希环的映射服务节点发生了变动: [{}]: [{}] -> [{}]", entry.getKey(), entry.getValue(), newServiceNode);
                }
            }
            // 通知部分客户端断开连接, 可以发一个全局广播让客户端断开，也可以服务端主动断开，其实就是这些客户端都要自动连接上新的实例
            //TODO reroute
//            fanoutSender.send(userIdClientsToReset);
        } else if (GlobalConstant.SERVER_DOWN_MESSAGE.equalsIgnoreCase(upOrDown)) {
            // 实例下线-根据物理节点ip, 服务端已经立刻主动断连，网关也要移除掉对应节点
            ServiceNode serviceNode = new ServiceNode(serverIp);
            consistentHashRouter.removeNode(serviceNode);
        }
        // 将最新的哈希环放到 Redis
        SortedMap<Long, VirtualNode> ring = consistentHashRouter.getRing();
//        redisTemplate.opsForHash().putAll(GlobalConstant.HASH_RING_REDIS, ring);
        RMap<Long, VirtualNode> rMap = redissonClient.getMap(GlobalConstant.HASH_RING_REDIS, JsonJacksonCodec.INSTANCE);
        rMap.putAll(ring);
        logger.info("【哈希环】实例上线之后为 {}", JsonMapper.INSTANCE.toJson(ring)); // NOSONAR
        return Action.CommitMessage;
    }
}
