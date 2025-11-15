package com.spring.DeliveryApp.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // وسيط بسيط (Simple Broker): لبث الرسائل من الخادم للعملاء.
        // /topic/drivers: مسار عام للسائقين (لبث الطلبات الجديدة)
        // /user: مسار خاص (لتحديثات الطلب والتتبع الفردية)
        registry.enableSimpleBroker("/topic", "/user");

        // مسار الوجهة من العميل إلى المتحكم (يُستخدم لتقديم العروض)
        registry.setApplicationDestinationPrefixes("/app");

        // البادئة للمسارات الخاصة بالمستخدمين
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // نقطة اتصال WebSocket/SockJS
        registry.addEndpoint("/ws")
                .withSockJS(); // لدعم المتصفحات القديمة
    }
}