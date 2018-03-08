package nl.jpoint.trojkaracer.car.domain.car;

/**
 * Interface to the engine of the car. The engine provides methods to update the settings like speed and the steering direction. Implementations
 * of this class should translate these methods to the actual hardware control calls.
 */
public interface CarEngine {

    /**
     * Updates the motor with the new speed.
     * @param speed the speed to set.
     */
    void updateMotor(final Speed speed);

    /**
     * Updates the steering direction; setting the direction to steer.
     * @param direction the direction (in degrees) to set.
     */
    void updateDirection(final Direction direction);

}
