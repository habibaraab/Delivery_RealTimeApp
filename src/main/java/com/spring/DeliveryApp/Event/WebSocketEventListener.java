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

    /**
     * معالجة حدث اتصال مستخدم جديد (عند فتح جلسة WebSocket)
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // معرف المستخدم يتم تخزينه في الجلسة عند عملية المصادقة الأولية (افتراضياً)
        String userId = (String) headerAccessor.getSessionAttributes().get(AuthChannelInterceptor.USER_ID_SESSION_ATTRIBUTE);


        if (userId != null) {
            int userIdInt = Integer.parseInt(userId);
            // تحديث حالة المستخدم إلى متصل ومتاح
            userService.updateUserAvailability(userIdInt, true);
        }
    }

    /**
     * معالجة حدث انقطاع اتصال مستخدم (عند إغلاق الجلسة)
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // استعادة معرف المستخدم من سمات الجلسة
        String userId = (String) headerAccessor.getSessionAttributes().get(AuthChannelInterceptor.USER_ID_SESSION_ATTRIBUTE);
        if (userId != null) {
            int userIdInt = Integer.parseInt(userId);

            // تحديث حالة المستخدم إلى غير متاح
            userService.updateUserAvailability(userIdInt, false);
            // ملاحظة: يمكن هنا إضافة منطق للتعامل مع الطلبات النشطة للسائق الذي انقطع اتصاله.
        }
    }
}