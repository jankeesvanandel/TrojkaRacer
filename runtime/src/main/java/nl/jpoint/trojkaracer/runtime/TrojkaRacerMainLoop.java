package nl.jpoint.trojkaracer.runtime;

import nl.jpoint.trojkaracer.ai.AIService;
import nl.jpoint.trojkaracer.ai.DesiredActions;
import nl.jpoint.trojkaracer.hardwareinterface.DirectionController;
import nl.jpoint.trojkaracer.hardwareinterface.SpeedController;
import nl.jpoint.trojkaracer.pid.Killable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;


/**
 * The main loop of the TrojkaRacer. It gathers the desired actions for speed and direction from the {@link AIService} and then applies those
 * through the specific controllers to the hardware.
 */
@Singleton
public class TrojkaRacerMainLoop implements Runnable, Killable {

    private static final int PERCENTAGE = 100;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrojkaRacerMainLoop.class);

    private final AIService aiService;
    private final SpeedController<Integer> speedController;
    private final DirectionController<Integer> directionController;

    private boolean isStopped;

    @Inject
    public TrojkaRacerMainLoop(final AIService aiService,
                               final SpeedController<Integer> speedController,
                               final DirectionController<Integer> directionController) {
        this.aiService = aiService;
        this.speedController = speedController;
        this.directionController = directionController;

        isStopped = false;
        LOGGER.debug("Created new {} instance.", getClass().getSimpleName());
    }

    @Override
    public void run() {
        try {
            LOGGER.debug("Retrieving new set points and applying them to the car controllers");
            final DesiredActions desiredActions = aiService.getDesiredActions();
            if (desiredActions != null && !isStopped) {
                final Double steeringSetPoint = desiredActions.getSteeringAction().getSteeringPosition() * PERCENTAGE;
                final Double throttleSetPoint = desiredActions.getThrottleAction().getThrottleAmount() * PERCENTAGE;

                LOGGER.info("Setting a new required speed of {} and a required direction of {}", throttleSetPoint.intValue(), steeringSetPoint.intValue());

                directionController.setDirection(steeringSetPoint.intValue());
                speedController.setSpeed(throttleSetPoint.intValue());
            }
        } catch (Exception e) {
            LOGGER.error("Error in main loop: ", e);
        }
    }

    @Override
    public void kill() {
        LOGGER.warn("Kill called on main loop!");
        isStopped = true;
        speedController.stop();
        directionController.stop();
    }
}
