package com.spring.DeliveryApp.auth.Security;

import com.spring.DeliveryApp.auth.Service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    // اسم السمة الذي سنستخدمه لتخزين معرف المستخدم
    public static final String USER_ID_SESSION_ATTRIBUTE = "userId";

//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//
//        // التحقق فقط عند محاولة الاتصال (CONNECT)
//        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//
//            // 1. استخراج JWT من رؤوس STOMP (العميل يرسله عادة في رأس "Authorization" أو رأس مخصص)
//            // نفترض هنا أن العميل يرسله كرأس "Authorization: Bearer <token>"
//            List<String> authorization = accessor.getNativeHeader("Authorization");
//
//            if (authorization != null && !authorization.isEmpty()) {
//                String token = authorization.get(0).substring(7); // إزالة "Bearer "
//                String username = jwtService.extractUsername(token);
//
//                if (username != null) {
//                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//
//                    // 2. التحقق من صحة الرمز المميز
//                    if (jwtService.isTokenValid(token, userDetails)) {
//
//                        // 3. إنشاء كائن Authentication
//                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
//                                userDetails,
//                                null,
//                                userDetails.getAuthorities()
//                        );
//
//                        // 4. تعيين Authentication للجلسة (يستخدمه Spring Security)
//                        accessor.setUser(auth);
//
//                        // 5. **الأهم:** تخزين معرف المستخدم في سمات الجلسة (لـ WebSocketEventListener)
//                        // هذا هو ما يحتاجه UserService لتحديد من قام بالاتصال/الانقطاع
//                        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
//                        sessionAttributes.put(USER_ID_SESSION_ATTRIBUTE, username);
//
//                        System.out.println("✅ تم مصادقة مستخدم WebSocket: " + username);
//                    }
//                }
//            }
//        }
//        return message;
//    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            // 1. قراءة الرمز المميز من سمات الجلسة (التي تم جلبها من URL في الخطوة 1)
            String token = (String) accessor.getSessionAttributes().get(HttpHandshakeInterceptor.TOKEN_KEY);

            if (token != null) {
                try {
                    String username = jwtService.extractUsername(token);

                    if (username != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        // 2. التحقق من صحة الرمز المميز
                        if (jwtService.isTokenValid(token, userDetails)) {

                            // 3. تعيين Authentication و userId لجلسة STOMP
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                            accessor.setUser(auth);
                            accessor.getSessionAttributes().put(USER_ID_SESSION_ATTRIBUTE, username);

                            System.out.println("✅ تم مصادقة مستخدم WebSocket (عبر URL): " + username);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ فشل مصادقة STOMP: " + e.getMessage());
                    // إذا فشلت المصادقة، يجب رفض الاتصال
                    return null;
                }
            } else {

                 return null;
            }
        }
        return message;
    }
}