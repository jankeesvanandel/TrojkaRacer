package nl.jpoint.trojkaracer.car.domain.car;

/**
 * Exception for when a driver does some illegal or erroneous driving.
 */
public class DrivingCarException extends RuntimeException{

    public DrivingCarException(final String message) {
        super(message);
    }
}
