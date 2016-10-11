package nl.jpoint.trojkaracer.hardwareinterfacing.servosteering;

import com.pi4j.io.gpio.GpioController;

/**
 * Implementation of the {@link ServoSteering} interface for a ModeCraft 4519 Servo motor.
 *
 * @see <a href="http://www.servodatabase.com/servo/modelcraft/4519">http://www.servodatabase.com/servo/modelcraft/4519</a>
 */
public class ModelCraft4519ServoSteering extends AbstractServoSteering {

    public ModelCraft4519ServoSteering(final GpioController gpioController) {
        super(gpioController, 800, 2100);
    }
}
