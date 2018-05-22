package nl.jpoint.trojkaracer.car.application;

import java.lang.invoke.MethodHandles;
import nl.jpoint.trojkaracer.car.domain.car.Car;
import nl.jpoint.trojkaracer.car.domain.car.CarStatus;
import nl.jpoint.trojkaracer.car.domain.driver.DriveCommand;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.SignalType;

/**
 * This is the general driving service. It provides a method to put a (new) Driver behind the steering wheel of the car and will
 * make sure all car status changes are send to the car status flux/processor.
 */
@Service
public class CarDrivingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Car car;
    private final Subscriber<CarStatus> carStatusSubscriber;

    private BaseSubscriber<DriveCommand> driver;
    private Subscription activeSubscription;

    /**
     * Creates and initializes the Car driving service.
     * @param car the car this car driving service controls.
     * @param carStatusSubscriber the subscriber to which new car status will need to be send.
     */
    public CarDrivingService(final Car car, final Subscriber<CarStatus> carStatusSubscriber) {
        LOGGER.info("Creating and initializing Car Driving Service...");

        this.car = car;
        this.carStatusSubscriber = carStatusSubscriber;
    }

    public CarStatus getCarStatus() {
        return car.getStatus();
    }

    /**
     * Puts a (new) driver behind the steering wheel of the car, one that will process the provided driver input. The new driver will now be
     * in control of the car. The possible previous driver will be signaled that its services are no longer required (read: the subscription
     * to the previous driver input will be disposed of).
     *
     * @param newDriverInput the driver input for the new driver.
     */
    public void putBehindWheel(final Publisher<DriveCommand> newDriverInput) {
        LOGGER.info("Putting a new driver ({}) behind the wheel of the car.", newDriverInput.getClass().getSimpleName());

        car.stop();

        if (activeSubscription != null) {
            activeSubscription.cancel();
        }

        driver = buildDriver();
        newDriverInput.subscribe(driver);

        car.stop();
        publishCarStatus();
    }

    private void processDriveCommand(final DriveCommand driveCommand) {
        driveCommand.applyCommandOnCar(car);
        publishCarStatus();
    }

    private BaseSubscriber<DriveCommand> buildDriver() {
        return new BaseSubscriber<DriveCommand>() {
            @Override
            protected void hookOnSubscribe(final Subscription s) {
                super.hookOnSubscribe(s);
                activeSubscription = s;
            }

            @Override
            protected void hookOnNext(final DriveCommand driveCommand) {
                super.hookOnNext(driveCommand);
                processDriveCommand(driveCommand);
            }

            @Override
            protected void hookOnCancel() {
                super.hookOnCancel();
                LOGGER.info("Cancelling subscription, so stopping the car.");
                car.stop();
            }

            @Override
            protected void hookFinally(final SignalType type) {
                super.hookFinally(type);

                LOGGER.warn("Car driver received termination event of type '{}', so stopping car immediately.", type);
                car.stop();
            }
        };
    }

    private void publishCarStatus() {
        carStatusSubscriber.onNext(getCarStatus());
    }

}
