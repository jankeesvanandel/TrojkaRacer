package nl.jpoint.trojkaracer.hardwareinterface;

/**
 * Exception raised when the pulse that is set to a PWM controlled Servo is set outside the its limits.
 */
public class PWMPulseOutsideLimitsException extends HardwareInterfaceException {

    public PWMPulseOutsideLimitsException(final String hardwareComponentName, final int value, final int lowerLimit, final int higherLimit) {
        super(String.format("Could not set the pulse width for the %s to %s as that is not between its limits of %s and %s.",
                hardwareComponentName, value, lowerLimit, higherLimit));
    }
}
