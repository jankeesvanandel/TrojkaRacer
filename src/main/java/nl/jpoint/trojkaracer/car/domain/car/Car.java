package nl.jpoint.trojkaracer.car.domain.car;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * General car class that provides methods to a driver to set the speed and steering direction.
 */
@Component("car")
public class Car {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int SPEED_STEP = 3;
    private static final int STEERING_STEP = 10;

    private final CarEngine carEngine;

    private Direction direction;
    private Speed speed;

    public Car(final CarEngine carEngine) {
        LOGGER.info("Creating and initializing the Trojka Racer Car...", this.getClass().getSimpleName());

        // Initialize the car state
        this.carEngine = carEngine;
        this.speed = new Speed(0);
        this.direction = new Direction(0);

        // Apply car state to hardware
        updateEngineSpeed();
        updateEngineSteeringDirection();
    }

    public void drive(final Speed newSpeed, final Direction newDirection) {
        final Direction calcDirection;
        if (newDirection == null) {
            calcDirection = direction;
        } else {
            calcDirection = newDirection;
        }

        if (newSpeed.getSpeedValue() == 0 && calcDirection.getDegrees() == 0) {
            stop();
        } else {
            if (allowedChange(newSpeed, calcDirection)) {
                speed = newSpeed;
                direction = calcDirection;
            } else {
                LOGGER.warn("Received a speed ('{}') and direction ('{}') that is not allowed to apply to the car with the current speed ('{}') and direction " +
                        "('{}').", newSpeed.getSpeedValue(), calcDirection.getDegrees(), speed.getSpeedValue(), direction.getDegrees());
                LOGGER.warn("Stopping the car.");
                stop();
            }
        }
        updateEngineSpeed();
        updateEngineSteeringDirection();
    }

    private boolean allowedChange(final Speed newSpeed, final Direction newDirection) {
        return (newSpeed.getSpeedValue() <= 0 && speed.getSpeedValue() <= 0 || newSpeed.getSpeedValue() >= 0 && speed.getSpeedValue() >= 0) &&
                Math.abs(newDirection.getDegrees() - direction.getDegrees()) <= Direction.MAX_DEGREES;
    }

    /**
     * Increases the speed by a single step. If the car has a negative speed (drives backwards), the increasing of the speed will slow the
     * car, even possibly stopping the car.
     * <p>If the car is already at its maximum speed, increasing the speed will do nothing.</p>
     */
    public void increaseSpeed() {
        speed = speed.increase(SPEED_STEP);
        updateEngineSpeed();
    }

    /**
     * Decreases the speed by a single step. If the car has a negative speed (drives backwards), the decreasing of the speed will let the car
     * drive faster backwards.
     * <p>If the car is already at its minimum speed, decreasomg the speed will do nothing.</p>
     */
    public void decreaseSpeed() {
        speed = speed.decrease(SPEED_STEP);
        updateEngineSpeed();
    }

    public void steerLeft() {
        direction = direction.steerLeft(STEERING_STEP);
        updateEngineSteeringDirection();
    }

    public void steerRight() {
        direction = direction.steerRight(STEERING_STEP);
        updateEngineSteeringDirection();
    }

    /**
     * Stops the car by immediately setting the speed to 0, placing the steering wheel in its neutral position and shifting to neutral.
     */
    public void stop() {
        speed = new Speed(0);
        direction = new Direction(0);

        updateEngineSpeed();
        updateEngineSteeringDirection();
    }

    private void updateEngineSpeed() {
        LOGGER.debug("Updating car engine speed: {}", speed);
        carEngine.updateMotor(speed);
    }

    private void updateEngineSteeringDirection() {
        LOGGER.debug("Updating car engine steering direction: {}", direction);
        if (direction != null) {
            carEngine.updateDirection(direction);
        }
    }

    /**
     * Returns the status of the car, including information regarding speed and steering direction.
     * @return the status of the car, including information regarding speed and steering direction.
     */
    public CarStatus getStatus() {
        return CarStatus.builder()
                .speed(this.speed)
                .direction(this.direction)
                .build();
    }
}
