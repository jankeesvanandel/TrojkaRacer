package nl.jpoint.trojkaracer.hardwareinterface;

/**
 * General Hardware Interface Exception for indicating an exception raised in the hardware interfacing.
 */
public class HardwareInterfaceException extends RuntimeException {

    public HardwareInterfaceException(final String messge) {
        super(messge);
    }

    public HardwareInterfaceException(final Exception exception) {
        super(exception);
    }
}
