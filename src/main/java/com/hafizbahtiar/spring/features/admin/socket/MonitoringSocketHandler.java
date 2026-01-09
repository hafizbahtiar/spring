package com.hafizbahtiar.spring.features.admin.socket;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hafizbahtiar.spring.features.admin.dto.HealthCheckResponse;
import com.hafizbahtiar.spring.features.admin.dto.SystemMetricsResponse;
import com.hafizbahtiar.spring.features.admin.service.AdminHealthService;
import com.hafizbahtiar.spring.features.admin.service.AdminMetricsService;
import com.hafizbahtiar.spring.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Socket.IO event handler for real-time monitoring.
 * Handles connections to the /monitoring namespace and broadcasts system
 * health/metrics updates.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MonitoringSocketHandler {

    @SuppressWarnings("unused")
    private final SocketIOServer socketIOServer;
    private final AdminHealthService adminHealthService;
    private final AdminMetricsService adminMetricsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    // Store authenticated clients
    private final Map<String, SocketIOClient> authenticatedClients = new ConcurrentHashMap<>();

    /**
     * Handle client connection to /monitoring namespace.
     * Validates JWT token and sends initial monitoring data.
     */
    @OnConnect
    public void onConnect(SocketIOClient client) {
        log.debug("Client attempting to connect: {}", client.getSessionId());
        log.debug("Handshake data: {}", client.getHandshakeData());

        // Get token from Socket.IO auth object (frontend sends token in auth.token)
        String token = null;

        // Method 1: Try to get from auth object (Socket.IO client sends auth: { token:
        // "..." })
        // In netty-socketio, auth data is sent as query parameters or in the handshake
        try {
            Object authTokenObj = client.getHandshakeData().getAuthToken();
            if (authTokenObj != null) {
                log.debug("Auth token object type: {}", authTokenObj.getClass().getName());
                if (authTokenObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> authMap = (Map<String, Object>) authTokenObj;
                    Object tokenObj = authMap.get("token");
                    if (tokenObj != null) {
                        token = tokenObj.toString();
                        log.debug("Extracted token from auth map");
                    }
                } else {
                    token = authTokenObj.toString();
                    log.debug("Extracted token from auth object");
                }
            }
        } catch (Exception e) {
            log.debug("Error extracting token from auth object: {}", e.getMessage());
        }

        // Method 2: Try to get from URL query parameter (token=...)
        if (token == null || token.isEmpty()) {
            token = client.getHandshakeData().getSingleUrlParam("token");
            if (token != null && !token.isEmpty()) {
                log.debug("Extracted token from URL parameter");
            }
        }

        // Method 3: Try to get from Authorization header
        if (token == null || token.isEmpty()) {
            try {
                Object authHeader = client.getHandshakeData().getHttpHeaders().get("Authorization");
                if (authHeader != null) {
                    String authHeaderStr = authHeader.toString();
                    if (authHeaderStr.startsWith("Bearer ")) {
                        token = authHeaderStr.substring(7);
                        log.debug("Extracted token from Authorization header");
                    }
                }
            } catch (Exception e) {
                log.debug("Error extracting token from Authorization header: {}", e.getMessage());
            }
        }

        // Method 4: Try to get from handshake query parameters (auth data might be in
        // query string)
        if (token == null || token.isEmpty()) {
            try {
                // Check if auth data is passed as query parameter
                String queryString = client.getHandshakeData().getUrl();
                if (queryString != null && queryString.contains("token=")) {
                    int tokenStart = queryString.indexOf("token=") + 6;
                    int tokenEnd = queryString.indexOf("&", tokenStart);
                    if (tokenEnd == -1) {
                        tokenEnd = queryString.length();
                    }
                    token = queryString.substring(tokenStart, tokenEnd);
                    log.debug("Extracted token from query string");
                }
            } catch (Exception e) {
                log.debug("Error extracting token from query string: {}", e.getMessage());
            }
        }

        log.debug("Final token value: {}",
                token != null && !token.isEmpty() ? "***" + token.substring(Math.max(0, token.length() - 4))
                        : "null/empty");

        // Validate token and authenticate
        if (token != null && !token.isEmpty() && jwtTokenProvider.validateToken(token)) {
            try {
                // Extract user info from token
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                String role = jwtTokenProvider.getRoleFromToken(token);

                // Check if user has owner/admin role (required for monitoring)
                if (role == null || (!role.equalsIgnoreCase("OWNER") && !role.equalsIgnoreCase("ADMIN"))) {
                    log.warn("Client {} attempted to connect without owner/admin role", client.getSessionId());
                    client.disconnect();
                    return;
                }

                // Store authenticated client
                authenticatedClients.put(client.getSessionId().toString(), client);
                log.info("Client {} connected successfully (userId: {}, role: {})", client.getSessionId(), userId,
                        role);

                // Send initial monitoring data
                sendInitialData(client);

            } catch (Exception e) {
                log.error("Error authenticating client {}: {}", client.getSessionId(), e.getMessage());
                client.disconnect();
            }
        } else {
            log.warn("Client {} attempted to connect without valid token", client.getSessionId());
            client.disconnect();
        }
    }

    /**
     * Handle client disconnection.
     */
    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        log.debug("Client disconnected: {}", client.getSessionId());
        authenticatedClients.remove(client.getSessionId().toString());
    }

    /**
     * Handle manual refresh request from client.
     */
    @OnEvent("monitoring:refresh")
    public void onRefreshRequest(SocketIOClient client, AckRequest ackRequest) {
        log.debug("Refresh request from client: {}", client.getSessionId());

        // Verify client is authenticated
        if (!authenticatedClients.containsKey(client.getSessionId().toString())) {
            log.warn("Unauthenticated client {} attempted to request refresh", client.getSessionId());
            if (ackRequest != null) {
                ackRequest.sendAckData(Map.of("error", "Unauthorized"));
            }
            return;
        }

        try {
            // Send updated data
            sendMonitoringUpdate(client);

            if (ackRequest != null) {
                ackRequest.sendAckData(Map.of("status", "success"));
            }
        } catch (Exception e) {
            log.error("Error handling refresh request: {}", e.getMessage(), e);
            if (ackRequest != null) {
                ackRequest.sendAckData(Map.of("error", "Failed to refresh data"));
            }
            sendError(client, "Failed to refresh monitoring data: " + e.getMessage());
        }
    }

    /**
     * Send initial monitoring data to a newly connected client.
     */
    private void sendInitialData(SocketIOClient client) {
        try {
            HealthCheckResponse systemHealth = adminHealthService.getSystemHealth();
            SystemMetricsResponse systemMetrics = adminMetricsService.getSystemMetrics();

            // Serialize DTOs to Maps using ObjectMapper to handle LocalDateTime properly
            Map<String, Object> data = new HashMap<>();
            data.put("systemHealth", objectMapper.convertValue(systemHealth, Map.class));
            data.put("systemMetrics", objectMapper.convertValue(systemMetrics, Map.class));
            data.put("timestamp", LocalDateTime.now().toString());

            client.sendEvent("monitoring:initial", data);
            log.debug("Sent initial monitoring data to client: {}", client.getSessionId());
        } catch (Exception e) {
            log.error("Error sending initial monitoring data: {}", e.getMessage(), e);
            sendError(client, "Failed to load initial monitoring data: " + e.getMessage());
        }
    }

    /**
     * Send monitoring update to a specific client.
     */
    private void sendMonitoringUpdate(SocketIOClient client) {
        try {
            HealthCheckResponse systemHealth = adminHealthService.getSystemHealth();
            SystemMetricsResponse systemMetrics = adminMetricsService.getSystemMetrics();

            // Serialize DTOs to Maps using ObjectMapper to handle LocalDateTime properly
            Map<String, Object> data = new HashMap<>();
            data.put("systemHealth", objectMapper.convertValue(systemHealth, Map.class));
            data.put("systemMetrics", objectMapper.convertValue(systemMetrics, Map.class));
            data.put("timestamp", LocalDateTime.now().toString());

            client.sendEvent("monitoring:update", data);
        } catch (Exception e) {
            log.error("Error sending monitoring update: {}", e.getMessage(), e);
            sendError(client, "Failed to update monitoring data: " + e.getMessage());
        }
    }

    /**
     * Send error notification to a client.
     */
    private void sendError(SocketIOClient client, String message) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("message", message);
        errorData.put("timestamp", LocalDateTime.now().toString());
        client.sendEvent("monitoring:error", errorData);
    }

    /**
     * Broadcast monitoring updates to all connected clients.
     * Runs every 30 seconds (configurable via cron expression).
     */
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void broadcastMonitoringUpdates() {
        if (authenticatedClients.isEmpty()) {
            return;
        }

        try {
            HealthCheckResponse systemHealth = adminHealthService.getSystemHealth();
            SystemMetricsResponse systemMetrics = adminMetricsService.getSystemMetrics();

            // Serialize DTOs to Maps using ObjectMapper to handle LocalDateTime properly
            Map<String, Object> data = new HashMap<>();
            data.put("systemHealth", objectMapper.convertValue(systemHealth, Map.class));
            data.put("systemMetrics", objectMapper.convertValue(systemMetrics, Map.class));
            data.put("timestamp", LocalDateTime.now().toString());

            // Broadcast to all authenticated clients
            for (SocketIOClient client : authenticatedClients.values()) {
                try {
                    client.sendEvent("monitoring:update", data);
                } catch (Exception e) {
                    log.debug("Failed to send update to client {}: {}", client.getSessionId(), e.getMessage());
                    // Remove disconnected client
                    authenticatedClients.remove(client.getSessionId().toString());
                }
            }
            log.debug("Broadcasted monitoring update to {} clients", authenticatedClients.size());
        } catch (Exception e) {
            log.error("Error broadcasting monitoring updates: {}", e.getMessage(), e);
        }
    }
}
