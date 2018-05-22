package nl.jpoint.trojkaracer.car.domain.car;

import lombok.Value;

/**
 * Value object representing the speed of the car.
 */
@Value
public class Speed {

    public static final int MAX_SPEED = 100;
    public static final int MIN_SPEED = -100;
    private static final int SPEED_MIN_POS_THRESHOLD = 15;
    private static final int SPEED_MIN_NEG_THRESHOLD = -15;

    private final int speedValue;

    /**
     * Creates a new {@link Speed} instance with the provided speed.
     * @param speed the speed of a car.
     * @throws DrivingCarException in case the speed is out of the speed range.
     */
    public Speed(final int speed) {
        if (speed < MIN_SPEED || speed > MAX_SPEED) {
            throw new DrivingCarException(String.format("Illegal speed (speed: %s)", speed));
        }
        this.speedValue = speed;
    }

    /**
     * Returns the speed as a percentage of the maximum speed.
     * @return the speed as a percentage of the maximum speed.
     */
    public int getSpeedAsPercentage() {
        if (speedValue < 0) {
            return (-100 * speedValue) / MIN_SPEED;
        } else {
            return (100 * speedValue) / MAX_SPEED;
        }
    }

    /**
     * Returns a new {@link Speed} instance with an increased speed; increased with the provided step (and minding the minimal speed threshold).
     * @param increaseSpeedStep the speed step the current speed has to be increased with.
     * @return a new {@link Speed} instance with a new (increased) speed.
     * @throws DrivingCarException if the speed was increased while that should not be allowed.
     */
    public Speed increase(final int increaseSpeedStep) {
        if (speedValue < MAX_SPEED) {
            int newSpeed = speedValue + increaseSpeedStep;
            if (newSpeed < 0 && newSpeed > SPEED_MIN_NEG_THRESHOLD) {
                newSpeed = 0;
            } else if (newSpeed > 0 && newSpeed < SPEED_MIN_POS_THRESHOLD) {
                newSpeed = SPEED_MIN_POS_THRESHOLD;
            }

            return new Speed(Math.min(MAX_SPEED, newSpeed));
        } else {
            return this;
        }
    }

    /**
     * Returns a new {@link Speed} instance with a decreased speed; decreased with the provided step.
     * @param decreaseSpeedStep the speed step the current speed has to be decreased with.
     * @return a new {@link Speed} instance with a new (decreased) speed.
     * @throws DrivingCarException if the speed was decreased while that should not be allowed.
     */
    public Speed decrease(final int decreaseSpeedStep) {
        if (speedValue > MIN_SPEED) {
            int newSpeed = speedValue - decreaseSpeedStep;
            if (newSpeed < 0 && newSpeed > SPEED_MIN_NEG_THRESHOLD) {
                newSpeed = SPEED_MIN_NEG_THRESHOLD;
            } else if (newSpeed > 0 && newSpeed < SPEED_MIN_POS_THRESHOLD) {
                newSpeed = 0;
            }
            return new Speed(Math.max(MIN_SPEED, newSpeed));
        } else {
            return this;
        }
    }
}
