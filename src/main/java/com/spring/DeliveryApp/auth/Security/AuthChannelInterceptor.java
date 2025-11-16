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

//Bridging HTTP to WebSocket (Secure jwt)
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public static final String USER_ID_SESSION_ATTRIBUTE = "userId";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // 1. Handle Initial Connection (CONNECT Command)
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            List<String> authorizationHeaders = accessor.getNativeHeader("Authorization");
            String token = null;

            if (authorizationHeaders != null && !authorizationHeaders.isEmpty()) {
                String fullToken = authorizationHeaders.get(0);
                if (fullToken != null && fullToken.startsWith("Bearer ")) {
                    token = fullToken.substring(7); // Extract token after "Bearer "
                }
            }

            if (token != null) {
                try {
                    String username = jwtService.extractUsername(token);

                    if (username != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        if (jwtService.isTokenValid(token, userDetails)) {

                            // 2. Create and Set Authentication Context
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                            accessor.setUser(auth); // Assign Principal to the STOMP session

                            // (The manual SecurityContextHolder setting was removed in the final stable fix, as accessor.setUser is generally enough
                            // when SecurityContextChannelInterceptor is also used or the logic is clean)

                            System.out.println("WebSocket user successfully authenticated: " + username);
                        } else {
                            System.err.println(" JWT token is invalid or expired.");
                            return null;
                        }
                    }
                } catch (Exception e) {
                    System.err.println(" STOMP authentication failed: " + e.getMessage());
                    return null;
                }
            } else {
                System.err.println(" Connection rejected: Authorization header missing or malformed.");
                return null;
            }
        }
        return message;
    }

}