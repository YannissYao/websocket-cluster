package me.yannis.websocket.event;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.vip.vjtools.vjkit.mapper.JsonMapper;
import me.yannis.websocket.server.WebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import world.oasis.base.constant.AppConfig;
import world.oasis.common.constant.GlobalConstant;
import world.oasis.common.model.MessageType;
import world.oasis.common.model.WebSocketMessage;

import java.io.UnsupportedEncodingException;

/**
 * 服务下线事件处理
 *
 * @author yannis
 * @since 2021/3/23
 */
@Component
public class ServerDownEventHandler implements ApplicationListener<ContextClosedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ServerDownEventHandler.class);

    @Autowired
    private ProducerBean producerBean;

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        logger.debug("当前 WebSocket 实例 - 准备下线 {}", contextClosedEvent.getApplicationContext().getDisplayName());
        logger.info("Redis 发布服务下线消息，通知网关移除相关节点");

        WebSocketMessage webSocketMessage = WebSocketMessage.toUserOrServerMessage(
                MessageType.FOR_SERVER, GlobalConstant.SERVER_DOWN_MESSAGE, WebSocketEndpoint.sessionMap.size());
        Message rocketMessage = null;
        try {
            rocketMessage = new Message(AppConfig.ROCKETMQ_OASIS_WEBSOCKET,
                    AppConfig.ROCKETMQ_TAG_WEBSOCKET_DOWN, null
                    , JsonMapper.INSTANCE.toJson(webSocketMessage).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        producerBean.sendOneway(rocketMessage);
        logger.info("服务实例开始主动断开所有 WebSocket 连接...");
        WebSocketEndpoint.disconnectAllByServer();
    }
}
