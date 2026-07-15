package com.prakash.clinicos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP over WebSocket for the live queue board.
 *
 * Why push instead of the receptionist screen polling GET /queue/today every
 * few seconds? Polling means N screens × 1 request every few seconds even
 * when nothing changed. Push means QueueService sends one message only when
 * a token actually changes state, and every subscribed screen (reception,
 * doctor dashboard, patient-facing display) updates instantly.
 *
 * Topic shape: /topic/clinics/{clinicId}/queue — clinic-scoped so one
 * clinic's queue events never leak to another tenant's screen.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // SockJS fallback for browsers/networks that block raw WebSocket upgrades
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Simple in-memory broker — sufficient for a single-instance deployment.
        // A multi-instance deployment would swap this for a STOMP relay (e.g. RabbitMQ).
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
