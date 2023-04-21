package me.yannis.websocket.event;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.vip.vjtools.vjkit.mapper.JsonMapper;
import me.yannis.websocket.server.WebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import world.oasis.base.constant.AppConfig;
import world.oasis.common.constant.GlobalConstant;
import world.oasis.common.model.MessageType;
import world.oasis.common.model.WebSocketMessage;

import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;

/**
 * 服务上线事件处理
 *
 * @author yannis
 * @since 2021/3/21
 */
@Component
public class ServerUpEventHandler implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ServerUpEventHandler.class);

    @Autowired
    private ProducerBean producerBean;


    @Override
    public void onApplicationEvent(@Nonnull ApplicationReadyEvent applicationReadyEvent) {
        logger.debug("当前 WebSocket 实例 - 准备就绪，即将发布上线消息. {}", applicationReadyEvent);
        try {
            // Sleep 是为了确保该实例 100% 准备好了
            Thread.sleep(5000);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
        logger.info("WebSocket 实例通过 Redis 发布服务上线消息：通知网关更新哈希环");


        WebSocketMessage webSocketMessage = WebSocketMessage.toUserOrServerMessage(
                MessageType.FOR_SERVER, GlobalConstant.SERVER_UP_MESSAGE, WebSocketEndpoint.sessionMap.size());
        Message rocketMessage = null;
        try {
            rocketMessage = new Message(AppConfig.ROCKETMQ_OASIS_WEBSOCKET,
                    AppConfig.ROCKETMQ_TAG_WEBSOCKET_UP, null
                    , JsonMapper.INSTANCE.toJson(webSocketMessage).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        producerBean.sendOneway(rocketMessage);
    }
}
