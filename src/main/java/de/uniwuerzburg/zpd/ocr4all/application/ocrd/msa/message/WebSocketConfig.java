/**
 * File:     WebSocketConfig.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.message
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     07.03.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.message;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures the application to send STOMP messages to WebSocket clients.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	/**
	 * The prefix to filter destinations targeting the message broker.
	 */
	@Value("${ocr-d.web-socket.topic.prefix}")
	private String topic;

	/**
	 * The end point.
	 */
	@Value("${ocr-d.web-socket.topic.end-point}")
	private String endpoint;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.messaging.simp.config.AbstractMessageBrokerConfiguration#
	 * configureMessageBroker(org.springframework.messaging.simp.config.
	 * MessageBrokerRegistry)
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker(topic);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.socket.config.annotation.
	 * WebSocketMessageBrokerConfigurationSupport#registerStompEndpoints(org.
	 * springframework.web.socket.config.annotation.StompEndpointRegistry)
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint(endpoint).setAllowedOriginPatterns("*");
		registry.addEndpoint(endpoint).setAllowedOriginPatterns("*").withSockJS();
	}
}
