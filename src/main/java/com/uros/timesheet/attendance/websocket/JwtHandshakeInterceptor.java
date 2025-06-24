package com.uros.timesheet.attendance.websocket;

import com.uros.timesheet.attendance.security.CustomUserDetailsService;
import com.uros.timesheet.attendance.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * WebSocket handshake interceptor that extracts and validates JWT token
 * from query parameters or Authorization headers. If valid, binds the
 * authenticated user to the WebSocket session.
 */
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final String TOKEN_PARAM = "token";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        String jwt = null;

        // 1. Extract JWT from query parameters (?token=...)
        URI uri = request.getURI();
        String query = uri.getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && TOKEN_PARAM.equals(pair[0])) {
                    jwt = pair[1].trim();
                    break;
                }
            }
        }

        // 2. If not present, try Authorization header
        if (jwt == null) {
            List<String> authHeaders = request.getHeaders().get(AUTH_HEADER);
            if (authHeaders != null && !authHeaders.isEmpty()) {
                String bearer = authHeaders.get(0);
                if (bearer != null && bearer.startsWith(BEARER_PREFIX)) {
                    jwt = bearer.substring(BEARER_PREFIX.length()).trim();
                }
            }
        }

        // 3. Validate JWT
        if (jwt == null || !jwtTokenProvider.validateToken(jwt)) {
            // Returning false cancels the WebSocket handshake
            return false;
        }

        // 4. Resolve and bind the authenticated user to the WebSocket session
        UUID userId = jwtTokenProvider.getUserIdFromJWT(jwt);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId.toString());
        attributes.put("user", userDetails);

        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
    }
}
