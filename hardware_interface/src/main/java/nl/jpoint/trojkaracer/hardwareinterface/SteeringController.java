package nl.jpoint.trojkaracer.hardwareinterface;

/**
 * Interface for the steering controller. This controller provides the steering on a hardware level.
 */
public interface SteeringController {

    int MIN_DEGREES = 0;
    int MAX_DEGREES = 180;

    /**
     * Directs the steering in the provided amount of degrees, where 0 is complete left and 180 is complete right.
     * @param degrees the amount of degrees to steer to.
     * @return the number of degrees the steering was previously set to.
     */
    int steerDegrees(int degrees);

    int getSteering();

}
