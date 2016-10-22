package nl.jpoint.trojkaracer.hardwareinterfacing.servosteering;

import com.pi4j.io.gpio.GpioController;

/**
 * Implementation of the {@link ServoSteering} interface for a Fitec FS5109M Servo motor.
 *
 * @see <a href="http://www.servodatabase.com/servo/feetech/fs5109m">http://www.servodatabase.com/servo/feetech/fs5109m</a>
 */
public class FeetechFS5109MServoSteering extends AbstractServoSteering implements ServoSteering {

    public FeetechFS5109MServoSteering(final GpioController gpioController) {
        super(gpioController, 1000, 2000);
    }
}
