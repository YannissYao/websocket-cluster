package me.yannis.websocket.config;

import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author yannis
 * @since 2021/3/19
 */
//@Configuration
public class WebSocketConfig {

//    @Bean("serverEndpointExporter")
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}
