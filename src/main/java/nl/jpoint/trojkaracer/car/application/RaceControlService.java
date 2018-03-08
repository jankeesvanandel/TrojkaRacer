package nl.jpoint.trojkaracer.car.application;

import java.lang.invoke.MethodHandles;
import nl.jpoint.trojkaracer.car.domain.driver.Driver;
import nl.jpoint.trojkaracer.car.domain.finisher.Finisher;
import nl.jpoint.trojkaracer.car.domain.race.RaceStatus;
import nl.jpoint.trojkaracer.car.domain.starter.Starter;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.SignalType;

/**
 * This is the general race control service.
 */
@Service
public class RaceControlService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CarDrivingService carDrivingService;
    private final Driver automatedDriver;
    private final Driver manualDriver;
    private final Subscriber<RaceStatus> raceStatusSubscriber;
    private final Starter starter;
    private final Finisher finisher;

    private Subscription activeStarterSubscription;
    private Subscription activeFinisherSubscription;

    private RaceStatus raceStatus;

    public RaceControlService(final CarDrivingService carDrivingService,
                              final Driver automatedDriver,
                              final Driver manualDriver,
                              final Subscriber<RaceStatus> raceStatusSubscriber,
                              final Starter starter,
                              final Finisher finisher) {

        LOGGER.info("Creating and initializing Race Control Service...");

        this.carDrivingService = carDrivingService;
        this.automatedDriver = automatedDriver;
        this.manualDriver = manualDriver;
        this.raceStatusSubscriber = raceStatusSubscriber;
        this.starter = starter;
        this.finisher = finisher;

        startManualDriving();
    }

    private BaseSubscriber<Boolean> buildNewStartSubscriber() {
        return new BaseSubscriber<Boolean>() {
            @Override
            protected void hookOnSubscribe(final Subscription s) {
                super.hookOnSubscribe(s);
                activeStarterSubscription = s;
            }

            @Override
            protected void hookOnNext(final Boolean signal) {
                super.hookOnNext(signal);
                LOGGER.info("Starter detected start signal.");
                startRace();
            }

            @Override
            protected void hookFinally(final SignalType type) {
                super.hookFinally(type);
                LOGGER.info("Starter received end signal '{}'.", type);
            }
        };
    }

    private BaseSubscriber<Boolean> buildNewFinishSubscriber() {
        return new BaseSubscriber<Boolean>() {
            @Override
            protected void hookOnSubscribe(final Subscription s) {
                super.hookOnSubscribe(s);
                activeFinisherSubscription = s;
            }

            @Override
            protected void hookOnNext(final Boolean signal) {
                super.hookOnNext(signal);
                LOGGER.info("Finisher detected the finish.");
                finishRace();
            }

            @Override
            protected void hookFinally(final SignalType type) {
                super.hookFinally(type);
                LOGGER.info("Finisher received end signal '{}'.", type);
            }
        };

    }

    public void startManualDriving() {
        LOGGER.info("Starting manual driving.");

        publishRaceStatus(RaceStatus.MANUAL_DRIVING);

        carDrivingService.putBehindWheel(manualDriver);
    }

    public void initRace() {
        LOGGER.info("Initializing the race; awaiting the start signal.");

        publishRaceStatus(RaceStatus.AWAITING_START_SIGNAL);

        starter.subscribe(buildNewStartSubscriber());
    }

    public void startRace() {
        LOGGER.info("Starting the race.");

        activeStarterSubscription.cancel();

        carDrivingService.putBehindWheel(automatedDriver);
        publishRaceStatus(RaceStatus.RACING);

        finisher.subscribe(buildNewFinishSubscriber());
    }

    private void finishRace() {
        if (!isRacing()) {
            LOGGER.info("Attempt to finish a race, while there was no race going on.");
            return;
        }

        LOGGER.info("Finishing the race.");

        activeFinisherSubscription.cancel();

        carDrivingService.putBehindWheel(manualDriver);
        publishRaceStatus(RaceStatus.MANUAL_DRIVING);
    }

    public void stopRace() {
        if (!isRacing()) {
            LOGGER.info("Attempt to stop a race, while there was no race going on.");
            return;
        }

        LOGGER.info("Stopping the race.");

        if (activeStarterSubscription != null) {
            activeStarterSubscription.cancel();
        }
        if (activeFinisherSubscription != null) {
            activeFinisherSubscription.cancel();
        }

        publishRaceStatus(RaceStatus.MANUAL_DRIVING);
        carDrivingService.putBehindWheel(manualDriver);
    }

    public void pauseRace() {
        if (!isRacing()) {
            LOGGER.info("Attempt to stop a race, while there was no race going on.");
            return;
        }

        LOGGER.info("Pausing the race.");

        carDrivingService.putBehindWheel(manualDriver);
        publishRaceStatus(RaceStatus.RACING_PAUSED);
    }

    public void continueRace() {
        if (!isRacing()) {
            LOGGER.info("Attempt to continue a race, while there was no race going on.");
            return;
        }

        LOGGER.info("Continuing the race.");

        carDrivingService.putBehindWheel(automatedDriver);
        publishRaceStatus(RaceStatus.RACING);
    }

    private void publishRaceStatus(final RaceStatus newRaceStatus) {
        raceStatus = newRaceStatus;
        raceStatusSubscriber.onNext(raceStatus);
    }

    private boolean isRacing() {
        return raceStatus == RaceStatus.RACING || raceStatus == RaceStatus.AWAITING_START_SIGNAL || raceStatus == RaceStatus.RACING_PAUSED;
    }

}
