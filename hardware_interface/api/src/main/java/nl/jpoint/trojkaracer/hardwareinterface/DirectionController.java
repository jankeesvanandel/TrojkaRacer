package nl.jpoint.trojkaracer.hardwareinterface;

/**
 * General interface to all direction controllers. It provides methods to set and retrieve the direction.
 * <p>
 *     Note that this is a very generic interface as it does not say anything about the actual values used to control the direction. The actual implementations
 *     define what needs to be used to control the direction, be it either a percentage, a pulse width, a double value between -1 and 1 or some other numeric
 *     representation.
 * </p>
 */
public interface DirectionController<T extends Number> extends Stoppable {

    /**
     * Sets the direction to the specified value.
     * @param direction the new direction.
     * @return the "old" direction before the new direction was set.
     * @throws PWMPulseOutsideLimitsException exception thrown when the direction that is set is outside the limits.
     */
    T setDirection(T direction);

    /**
     * Returns the current steering direction.
     * @return the current steering direction.
     */
    T getDirection();

}
