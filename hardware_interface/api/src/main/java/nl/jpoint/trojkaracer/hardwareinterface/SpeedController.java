package nl.jpoint.trojkaracer.hardwareinterface;

/**
 * General interface to all speed controllers. It provides methods to set and retrieve the speed.
 * <p>
 *     Note that this is a very generic interface as it does not say anything about the actual values used to control the speed. The actual implementations
 *     define what needs to be used to control the speed, be it either a percentage, a pulse width, a double value between -1 and 1 or some other numeric
 *     representation.
 * </p>
 */
public interface SpeedController<T extends Number> extends Stoppable {

    /**
     * Sets the speed to the specified value.
     * @param speed the new speed.
     * @return the "old" speed before the new speed was set.
     * @throws PWMPulseOutsideLimitsException exception thrown when the speed that is set is outside the limits (too low, or too high).
     */
    T setSpeed(T speed);

    /**
     * Returns the current speed.
     * @return the current speed.
     */
    T getSpeed();

}
