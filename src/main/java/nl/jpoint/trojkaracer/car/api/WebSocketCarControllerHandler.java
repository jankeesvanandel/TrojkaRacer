package nl.jpoint.trojkaracer.car.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import nl.jpoint.trojkaracer.car.domain.car.CarStatus;
import nl.jpoint.trojkaracer.car.domain.driver.DriveCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controller class for controlling through a websocket sent events.
 */
public class WebSocketCarControllerHandler implements WebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Flux<CarStatus> carStatusFlux;
    private final ObjectMapper objectMapper;
    private final CoreSubscriber<DriveCommand> manualDriveCommandProcessor;

    private CarStatus currentCarStatus;

    public WebSocketCarControllerHandler(final Flux<CarStatus> carStatusFlux,
                                         final ObjectMapper objectMapper,
                                         final CoreSubscriber<DriveCommand> manualDriveCommandProcessor) {
        LOGGER.debug("Creating new {}", this.getClass().getSimpleName());

        this.carStatusFlux = carStatusFlux;
        this.objectMapper = objectMapper;
        this.manualDriveCommandProcessor = manualDriveCommandProcessor;
    }

    @Override
    public Mono<Void> handle(final WebSocketSession session) {
        LOGGER.debug("Connecting new session with id '{}' to the {}.", session.getId(), getClass().getSimpleName());

        // Setup message handler for receiving messages through the websocket
        session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(this::toEvent)
                .map(CarControlEvent::toDriveCommand)
                .subscribe(manualDriveCommandProcessor);

        // Setup the flux to send messages over the websocket
        return session.send(carStatusFlux
                .doOnNext(carStatus -> this.currentCarStatus = carStatus)
                .map(this::toJSON)
                .map(session::textMessage));
    }

    private String toJSON(final CarStatus carStatus) {
        LOGGER.trace("Converting car status event to json.");
        try {
            return objectMapper.writeValueAsString(carStatus);
        } catch (final JsonProcessingException e) {
            LOGGER.error("Failed to map car status event: {}", carStatus, e);
            throw new RuntimeException(e);
        }
    }

    private CarControlEvent toEvent(final String message) {
        try {
            return objectMapper.readValue(message, CarControlEvent.class);
        } catch (final IOException mappingException) {
            LOGGER.error("Failed to map car control message to event: {}", message, mappingException);
            throw new RuntimeException(mappingException);
        }
    }
}
