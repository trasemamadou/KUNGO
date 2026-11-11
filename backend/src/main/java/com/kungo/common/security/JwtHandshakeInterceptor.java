package com.kungo.common.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * Authentifie la poignée de main WebSocket via le JWT transmis en query param
 * ({@code /ws?token=...}), le navigateur ne pouvant pas envoyer d'en-tête
 * {@code Authorization} lors du handshake SockJS. L'id utilisateur résolu est
 * placé dans les attributs de session STOMP pour router les messages privés
 * (`/user/queue/notifications`).
 */
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final String TOKEN_PARAM = "token";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }
        HttpServletRequest httpRequest = servletRequest.getServletRequest();
        String token = httpRequest.getParameter(TOKEN_PARAM);

        if (!StringUtils.hasText(token) || !jwtTokenProvider.isTokenValid(token)) {
            return false;
        }
        attributes.put("userId", jwtTokenProvider.extractUserId(token).toString());
        attributes.put("email", jwtTokenProvider.extractEmail(token));
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
