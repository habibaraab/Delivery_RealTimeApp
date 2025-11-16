package com.spring.DeliveryApp.Event;

import com.spring.DeliveryApp.Service.UserService;
import com.spring.DeliveryApp.auth.Security.AuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final UserService userService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // Fetch the username from Principal (which was set by AuthChannelInterceptor)
        String userId = null;
        if (headerAccessor.getUser() != null) {
            userId = headerAccessor.getUser().getName();
        }

        if (userId != null) {
            userService.updateUserAvailability(userId, true);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String userId = null;
        if (headerAccessor.getUser() != null) {
            userId = headerAccessor.getUser().getName();
        }

        if (userId != null) {
            userService.updateUserAvailability(userId, false);
        }
    }
}