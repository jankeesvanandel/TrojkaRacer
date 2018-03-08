package nl.jpoint.trojkaracer.car.api;

import java.lang.invoke.MethodHandles;
import nl.jpoint.trojkaracer.car.domain.driver.DriveCommand;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.CoreSubscriber;

/**
 * Subscriber class that subscribes to the manual driving commands.
 * @deprecated this class is no longer used as the subscription to the car control events that come in through the web are handled by
 *             the ManualDriveCommandProcessor.
 */
class CarControlMessageSubscriber implements CoreSubscriber<CarControlEvent>  {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Subscriber<DriveCommand> manualDriveCommandSubscriber;

    CarControlMessageSubscriber(final Subscriber<DriveCommand> manualDriveCommandSubscriber) {
        LOGGER.debug("Creating new {}", this.getClass().getSimpleName());

        this.manualDriveCommandSubscriber = manualDriveCommandSubscriber;
    }

    @Override
    public void onSubscribe(final Subscription subscription) {
        LOGGER.debug("External client subscribed to car control.");
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(final CarControlEvent carControlEvent) {
        LOGGER.debug("RaceControlMessageSubscriber handling onNext with message '{}'.", carControlEvent);
        manualDriveCommandSubscriber.onNext(carControlEvent.toDriveCommand());
    }

    @Override
    public void onError(final Throwable t) {
        LOGGER.error("CarControlMessageSubscriber handling onError.", t);
    }

    @Override
    public void onComplete() {
        LOGGER.debug("CarControlMessageSubscriber handling onComplete.");
    }

}
