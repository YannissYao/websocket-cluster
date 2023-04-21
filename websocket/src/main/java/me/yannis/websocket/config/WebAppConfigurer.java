package me.yannis.websocket.config;

import me.yannis.websocket.controller.RoomWebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
public class WebAppConfigurer extends WebMvcConfigurerAdapter implements WebSocketConfigurer {


    //    @Autowired
//    private RestTemplate restTemplate;
    @Autowired
    private RoomWebSocketController testWebSocketController;
//    @Autowired
//    private JwtSignature jwtSignature;
//    @Autowired
//    private SessionStorageDAO sessionStorageDAO;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

//        registry.addInterceptor(new SessionInterceptor(restTemplate)).addPathPatterns("/service/**").excludePathPatterns("/swagger-ui.html", "/swagger-resources/**", "/v2/**", "/webjars/**");


        super.addInterceptors(registry);
    }


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(testWebSocketController, "/room")
                .addInterceptors(new RoomHandShakeInterceptor()).setAllowedOrigins("*");
    }

}
