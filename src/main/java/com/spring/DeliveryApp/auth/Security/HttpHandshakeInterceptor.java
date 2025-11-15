package com.spring.DeliveryApp.auth.Security;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Component
public class HttpHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    public static final String TOKEN_KEY = "token"; // المفتاح الذي سنستخدمه لحفظ الرمز المميز

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        if (request instanceof org.springframework.http.server.ServletServerHttpRequest) {
            org.springframework.http.server.ServletServerHttpRequest servletRequest =
                    (org.springframework.http.server.ServletServerHttpRequest) request;

            // 1. قراءة معامل URL (Query Parameter) المسمى 'token'
            String token = servletRequest.getServletRequest().getParameter(TOKEN_KEY);

            if (token != null && !token.isEmpty()) {
                // 2. تخزين الرمز المميز في سمات الجلسة ليستخدمه مُعترض القناة لاحقاً
                attributes.put(TOKEN_KEY, token);
                System.out.println("✅ تم التقاط JWT من URL بنجاح.");
            } else {
                // يمكن هنا قراءة رأس Authorization كخيار احتياطي، لكن التركيز على URL أفضل
                System.out.println("⚠️ لم يتم العثور على رمز مميز في معامل URL.");
            }
        }
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }
}