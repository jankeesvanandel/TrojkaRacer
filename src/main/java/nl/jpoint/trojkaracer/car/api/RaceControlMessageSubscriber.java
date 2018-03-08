package nl.jpoint.trojkaracer.car.api;

import java.lang.invoke.MethodHandles;
import nl.jpoint.trojkaracer.car.application.RaceControlService;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.CoreSubscriber;

/**
 * Subscriber class that subscribes to the through the web incoming race control messages.
 */
class RaceControlMessageSubscriber implements CoreSubscriber<RaceControlEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final RaceControlService raceControlService;

    RaceControlMessageSubscriber(final RaceControlService raceControlService) {
        LOGGER.debug("Creating new {}", this.getClass().getSimpleName());

        this.raceControlService = raceControlService;
    }

    @Override
    public void onSubscribe(final Subscription subscription) {
        LOGGER.debug("External client subscribed to race control.");
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(final RaceControlEvent raceControlEvent) {
        LOGGER.debug("RaceControlMessageSubscriber handling onNext with message '{}'.", raceControlEvent);
        switch (raceControlEvent.getType()) {
            case MANUAL:
                raceControlService.startManualDriving();
                break;
            case INIT_RACE:
                raceControlService.initRace();
                break;
            case RACE:
                raceControlService.startRace();
                break;
            case STOP:
                raceControlService.stopRace();
                break;
            case PAUSE:
                raceControlService.pauseRace();
                break;
            case CONTINUE:
                raceControlService.continueRace();
                break;
        }
    }

    @Override
    public void onError(final Throwable t) {
        LOGGER.error("RaceControlMessageSubscriber handling onError.", t);
    }

    @Override
    public void onComplete() {
        LOGGER.debug("RaceControlMessageSubscriber handling onComplete.");
    }

}
