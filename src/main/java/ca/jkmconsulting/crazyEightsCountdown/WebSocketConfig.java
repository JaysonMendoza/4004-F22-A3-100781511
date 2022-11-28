package ca.jkmconsulting.crazyEightsCountdown;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@AllArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final Logger LOG = LoggerFactory.getLogger(WebSocketConfig.class);

    @Autowired
    private PlayerHandshakeHandler pHH;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        LOG.info("Setting up message broker.");
        config.enableSimpleBroker("/topic/","/queue/","/user/"); //messages sent to endpoints prefixed with /topic should be sent to the client
        config.setApplicationDestinationPrefixes("/app"); //messages sent to /app endpoints are intended for functions mapped to endpoints.
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        LOG.info("Registering websocket stomp endpoints");
        registry.addEndpoint("/crazy-eights-websocket").setHandshakeHandler(pHH).setAllowedOriginPatterns("*").withSockJS(); //Items sent to this STOMP endpoint are received by websocket
    }
}
