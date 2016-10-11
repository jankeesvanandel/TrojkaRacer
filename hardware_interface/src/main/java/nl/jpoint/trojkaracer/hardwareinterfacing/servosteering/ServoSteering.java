package nl.jpoint.trojkaracer.hardwareinterfacing.servosteering;


/**
 * Interface for steering (the car) with a Servo Motor. It provides methods to center the steering or turn/steer in a certain direction, where the
 * direction is defined in the range between 0 and 1000.
 */
public interface ServoSteering {

    int MAX_STEERING_FACTOR = 1000;
    int MIN_STEERING_FACTOR = 0;
    int CENTER_STEERING_FACTOR = 500;
    int DEFAULT_CLOCK_FREQUENCY = 19200000;

    /**
     * Puts the steer in the central position (at 0 degrees).
     */
    void center();

    /**
     * Sets the steering direction on the Servo Motor to a value between 0 and 1000, where 0 is completely to the left and 1000 completely to the right.
     * @param direction the steering direction in a range between 0 (full left) or 1000 (full right).
     */
    void steer(int direction);

}
