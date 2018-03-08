package nl.jpoint.trojkaracer.car.domain.car;

import lombok.Value;

/**
 * Value object for the direction of the car; the amount of degrees the steering wheel should be turned.
 */
@Value
public final class Direction {

    public static final int MIN_DEGREES = -60;
    public static final int MAX_DEGREES = 60;

    private final int degrees;

    /**
     * Creates a new {@link Direction} instance with the provided amount of degrees.
     * @param degrees the number of degrees the wheel should be turned.
     * @throws DrivingCarException in case the number of degrees is out of the steering range.
     */
    public Direction(final int degrees) throws DrivingCarException {
        if (degrees < MIN_DEGREES || degrees > MAX_DEGREES) {
            throw new DrivingCarException(String.format("Illegal number of degrees for steering wheel (degrees: %s)", degrees));
        }
        this.degrees = degrees;
    }

    /**
     * Returns a new {@link Direction} instance with a, by the step provided, increased amount of degrees.
     * <p>Note that the amount of degrees can not be increased above the maximal amount of degrees.</p>
     *
     * @param increaseDegreesStep the amount of degrees to increase the steering wheel with.
     * @return thew new steering wheel degrees
     * @throws DrivingCarException in case the number of degrees out of the steering range.
     */
    public Direction steerRight(final int increaseDegreesStep) throws DrivingCarException {
        if (degrees < MAX_DEGREES) {
            final int newDegrees = degrees + increaseDegreesStep;
            return new Direction(Math.min(MAX_DEGREES, newDegrees));
        } else {
            return this;
        }
    }

    /**
     * Returns a new {@link Direction} instance with a, by the step provided, decreased amount of degrees.
     * <p>Note that the amount of degrees can not be decreased below the minimal amount of degrees.</p>
     *
     * @param decreaseDegreesStep the amount of degrees to decrease the steering wheel with.
     * @return thew new steering wheel degrees
     * @throws DrivingCarException in case the number of degrees out of the steering range.
     */
    public Direction steerLeft(final int decreaseDegreesStep) throws DrivingCarException {
        if (degrees > MIN_DEGREES) {
            final int newDegrees = degrees - decreaseDegreesStep;
            return new Direction(Math.max(MIN_DEGREES, newDegrees));
        } else {
            return this;
        }
    }

    /**
     * Returns the direction as a percentage of the maximum or minimum direction.
     * @return the direction as a percentage of the maximum or minimum direction.
     */
    public int getDegreesAsPercentage() {
        if (degrees < 0) {
            return (-100 * degrees) / MIN_DEGREES;
        } else {
            return (100 * degrees) / MAX_DEGREES;
        }
    }
}
