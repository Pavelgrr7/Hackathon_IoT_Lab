package com.tst.iotlab.video;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private VideoStreamHandler videoStreamHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //todo сделать нормальный путь
        registry.addHandler(videoStreamHandler, "C:\\Users\\Pavel\\IdeaProjects\\iotlab\\src\\main\\resources\\video").setAllowedOrigins("*");
    }
}