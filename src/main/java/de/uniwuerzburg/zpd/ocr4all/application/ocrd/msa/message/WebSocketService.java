/**
 * File:     WebSocketService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.message
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     07.03.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.message;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.communication.message.spi.EventSPI;

/**
 * Defines WebSocket services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Service
public class WebSocketService {
	/**
	 * The message template for sending messages to the registered clients.
	 */
	private final SimpMessagingTemplate simpMessagingTemplate;

	/**
	 * The target destination.
	 */
	private final String destination;

	/**
	 * Creates a WebSocket service.
	 * 
	 * @param simpMessagingTemplate The message template for sending messages to the
	 *                              registered clients.
	 * @param topic                 The prefix to filter destinations targeting the
	 *                              message broker.
	 * @param endpoint              The end point.
	 * @since 17
	 */
	public WebSocketService(SimpMessagingTemplate simpMessagingTemplate,
			@Value("${ocr-d.web-socket.topic.prefix}") String topic,
			@Value("${ocr-d.web-socket.topic.end-point}") String endpoint) {
		super();

		this.simpMessagingTemplate = simpMessagingTemplate;

		destination = topic + endpoint;
	}

	/**
	 * Broadcast the event to the registered clients.
	 * 
	 * @param event The event to broadcast.
	 * @since 17
	 */
	public void broadcast(EventSPI event) {
		simpMessagingTemplate.convertAndSend(destination, event);
	}

}
