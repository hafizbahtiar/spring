package com.hafizbahtiar.spring.config;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Role;

/**
 * Socket.IO server configuration for real-time monitoring.
 */
@org.springframework.context.annotation.Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Slf4j
public class SocketIOConfig {

    @Value("${socketio.hostname:0.0.0.0}")
    private String hostname;

    @Value("${socketio.port:9092}")
    private Integer port;

    @Value("${socketio.context:/socket.io}")
    private String context;

    @Value("${socketio.ping-timeout:60000}")
    private int pingTimeout;

    @Value("${socketio.ping-interval:25000}")
    private int pingInterval;

    @Value("${socketio.max-frame-payload-length:10485760}")
    private int maxFramePayloadLength;

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:3001}")
    private String allowedOrigins;

    private SocketIOServer server;

    @Bean
    @Lazy
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setHostname(hostname);
        config.setPort(port);
        config.setContext(context);
        config.setPingTimeout(pingTimeout);
        config.setPingInterval(pingInterval);
        config.setMaxFramePayloadLength(maxFramePayloadLength);

        // CORS configuration - allow all origins for development
        // In production, configure specific origins
        config.setOrigin("*");
        config.setAllowCustomRequests(true);

        server = new SocketIOServer(config);

        // Add monitoring namespace
        server.addNamespace("/monitoring");

        try {
            server.start();
            log.info("Socket.IO server started on {}:{}{}", hostname, port, context);
            log.info("Monitoring namespace available at /monitoring");
        } catch (Exception e) {
            log.error("Failed to start Socket.IO server on port {}: {}", port, e.getMessage());
            if (e.getCause() instanceof java.net.BindException) {
                log.error("Port {} is already in use. Please stop the previous instance or change the port.", port);
                log.error("To find and kill the process using port {}: lsof -ti:{} | xargs kill -9", port, port);
            }
            throw new RuntimeException("Failed to start Socket.IO server", e);
        }

        return server;
    }

    /**
     * Enable Spring annotation scanning for Socket.IO event handlers.
     * Made static to avoid BeanPostProcessor warnings.
     * Uses @Lazy to defer initialization until after all BeanPostProcessors are
     * registered.
     */
    @Bean
    @DependsOn("socketIOServer")
    public static SpringAnnotationScanner springAnnotationScanner(@Lazy SocketIOServer socketIOServer) {
        return new SpringAnnotationScanner(socketIOServer);
    }

    /**
     * Stop Socket.IO server on application shutdown.
     */
    @PreDestroy
    public void stopSocketIOServer() {
        if (server != null) {
            log.info("Stopping Socket.IO server...");
            server.stop();
            log.info("Socket.IO server stopped");
        }
    }
}
