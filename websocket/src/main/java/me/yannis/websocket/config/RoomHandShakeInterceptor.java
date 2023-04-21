package me.yannis.websocket.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;
import java.util.Objects;

//https://www.cnblogs.com/strugglion/p/10021173.html
@Slf4j
public class RoomHandShakeInterceptor extends HttpSessionHandshakeInterceptor {


//    private JwtSignature jwtSignature;
//    private SessionStorageDAO sessionStorageDAO;
//
//    public RoomHandShakeInterceptor(JwtSignature jwtSignature, SessionStorageDAO sessionStorageDAO) {
//        this.jwtSignature = jwtSignature;
//        this.sessionStorageDAO = sessionStorageDAO;
//    }

    /*
     * 在WebSocket连接建立之前的操作，以鉴权为例
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        log.info("Handle before webSocket connected. ");

        // 获取url传递的参数，通过attributes在Interceptor处理结束后传递给WebSocketHandler
        // WebSocketHandler可以通过WebSocketSession的getAttributes()方法获取参数
        ServletServerHttpRequest serverRequest = (ServletServerHttpRequest) request;
        String token = serverRequest.getServletRequest().getParameter("token");
//        if (Strings.isNotBlank(token) && token.startsWith("Bearer ")) {
//            token = token.substring(7);
//        } else {
//            log.error("Validation failed. WebSocket will not connect. ");
//            return false;
//        }
//        JwtUtil.JwtOptions jwtOptions;
//        try {
//            jwtOptions = jwtSignature.decode(token);
//        } catch (Exception var9) {
//            log.error("Validation failed. WebSocket will not connect. ");
//            return false;
//        }
//        if (Objects.isNull(jwtOptions)) {
//            log.error("Validation failed. WebSocket will not connect. ");
//            return false;
//        }
//        attributes.put("uid", jwtOptions.getCid());
        attributes.put("uid", token);

        log.info("Validation passed. WebSocket connecting.... ");
//        attributes.put("id", id);
//        attributes.put("name", name);
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception ex) {
        // 省略
    }
}
