package nl.jpoint.trojkaracer.hardwareinterfacing.servosteering;

import com.pi4j.io.gpio.GpioController;

/**
 * Implementation of the {@link ServoSteering} interface for a Tower Pro SG90 Servo motor.
 *
 * @see <a href="http://www.servodatabase.com/servo/towerpro/sg90">http://www.servodatabase.com/servo/towerpro/sg90</a>
 */
public class SG90ServoSteering extends AbstractServoSteering implements ServoSteering {

    public SG90ServoSteering(final GpioController gpioController) {
        super(gpioController, 500, 2400);
    }

}
