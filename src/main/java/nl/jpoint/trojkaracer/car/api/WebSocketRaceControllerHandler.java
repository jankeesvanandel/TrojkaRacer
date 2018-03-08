package nl.jpoint.trojkaracer.car.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import nl.jpoint.trojkaracer.car.application.RaceControlService;
import nl.jpoint.trojkaracer.car.domain.race.RaceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controller class for controlling the race status through websocket sent events.
 */
public class WebSocketRaceControllerHandler implements WebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Flux<RaceStatus> raceStatusFlux;
    private final ObjectMapper objectMapper;
    private final RaceControlService raceControlService;

    public WebSocketRaceControllerHandler(final Flux<RaceStatus> raceStatusFlux,
                                          final ObjectMapper objectMapper,
                                          final RaceControlService raceControlService) {
        LOGGER.debug("Creating new {}", this.getClass().getSimpleName());

        this.raceControlService = raceControlService;
        this.objectMapper = objectMapper;
        this.raceStatusFlux = raceStatusFlux;
    }

    @Override
    public Mono<Void> handle(final WebSocketSession session) {
        LOGGER.debug("Connecting new session with id '{}' to the {}.", session.getId(), getClass().getSimpleName());

        // Setup message handler for receiving messages through the websocket
        session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(this::toEvent)
                .subscribe(new RaceControlMessageSubscriber(raceControlService));

        // Setup the flux to send messages over the websocket
        return session.send(raceStatusFlux
                .map(this::toJSON)
                .map(session::textMessage));
    }

    private String toJSON(final RaceStatus raceStatus) {
        LOGGER.trace("Converting race status event to json.");
        try {
            return objectMapper.writeValueAsString(raceStatus);
        } catch (final JsonProcessingException mappingException) {
            LOGGER.error("Failed to map race status event to JSON: {}", raceStatus, mappingException);
            throw new RuntimeException(mappingException);
        }
    }

    private RaceControlEvent toEvent(final String message) {
        try {
            return objectMapper.readValue(message, RaceControlEvent.class);
        } catch (final IOException mappingException) {
            LOGGER.error("Failed to map race control message to event: {}", message, mappingException);
            throw new RuntimeException(mappingException);
        }
    }
}
