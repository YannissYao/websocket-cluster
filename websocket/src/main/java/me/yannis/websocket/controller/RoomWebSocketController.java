package me.yannis.websocket.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
public class RoomWebSocketController implements WebSocketHandler {

    private static AtomicInteger onlineCount = new AtomicInteger(0);

    public static final ConcurrentMap<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>(); // NOSONAR
    public static final ConcurrentMap<String, CopyOnWriteArraySet<Long>> roomMap = new ConcurrentHashMap<>();

    //wss://develop.oasis.world/room?token=Bearer
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        int onlineNum = addOnlineCount();
        log.info("Oprn a WebSocket. Current connection number: " + onlineNum);
        sessionMap.put(Long.parseLong(String.valueOf(session.getAttributes().get("uid"))), session);
//        session.sendMessage(new TextMessage("conn_success"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        //主动断开
        this.offLine(session);
        int onlineNum = subOnlineCount();
        log.info("Close a webSocket. Current connection number: " + onlineNum);
    }

    @Override
    public void handleMessage(WebSocketSession wsSession, WebSocketMessage<?> message) throws Exception {

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Exception occurs on webSocket connection. disconnecting....");
        if (session.isOpen()) {
            session.close();
        }
        this.offLine(session);
        subOnlineCount();
    }

    /*
     * 是否支持消息拆分发送：如果接收的数据量比较大，最好打开(true), 否则可能会导致接收失败。
     * 如果出现WebSocket连接接收一次数据后就自动断开，应检查是否是这里的问题。
     */
    @Override
    public boolean supportsPartialMessages() {
        return true;
    }


    public static int getOnlineCount() {
        return onlineCount.get();
    }

    public static int addOnlineCount() {
        return onlineCount.incrementAndGet();
    }

    public static int subOnlineCount() {
        return onlineCount.decrementAndGet();
    }

    public void offLine(WebSocketSession webSocketSession) {

    }
}
