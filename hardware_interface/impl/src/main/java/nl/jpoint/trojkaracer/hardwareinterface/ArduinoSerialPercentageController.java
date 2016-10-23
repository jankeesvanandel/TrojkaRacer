package nl.jpoint.trojkaracer.hardwareinterface;

import nl.jpoint.trojkaracer.hardwareinterface.adapter.ArduinoSerialCommandAdapter;
import nl.jpoint.trojkaracer.hardwareinterface.adapter.PWMValues;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Speed and direction controller implementation that communicates over a Serial line to an Arduino. The speed and direction are set (and returned) as a
 * positive or negative percentage (ranging from -100 to +100; -50% speed means backwards at half the speed).
 * <p>
 *     This controller is a wrapper around the {@link ArduinoSerialCommandAdapter}, which is compatible with both the
 *     <tt>arduino_serial_servo_control</tt> and <tt>arduino_serial_servo_control_buffered</tt> scripts.
 * </p>
 */
@Singleton
public class ArduinoSerialPercentageController implements SpeedController<Integer>, DirectionController<Integer>, Stoppable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArduinoSerialPercentageController.class);

    private final ArduinoSerialCommandAdapter arduinoSerialCommandAdapter;
    private final PWMValues speedPWMValues;
    private final PWMValues directionPWMValues;

    @Inject
    public ArduinoSerialPercentageController(final ArduinoSerialCommandAdapter arduinoSerialCommandAdapter,
                                             final PWMValues speedPWMValues,
                                             final PWMValues directionPWMValues) {
        this.arduinoSerialCommandAdapter = arduinoSerialCommandAdapter;
        this.speedPWMValues = speedPWMValues;
        this.directionPWMValues = directionPWMValues;

        LOGGER.info("Created {} instance", this.getClass().getSimpleName());
    }

    @Override
    public Integer setSpeed(final Integer speed) {
        final int newPulseWidth = speedPWMValues.getPulseWidthForPercentage(speed);
        final int oldPulseWidth = arduinoSerialCommandAdapter.setSpeed(newPulseWidth);

        return speedPWMValues.getPercentageForPulseWidth(oldPulseWidth);
    }

    @Override
    public Integer getSpeed() {
        return speedPWMValues.getPercentageForPulseWidth(arduinoSerialCommandAdapter.getSpeed());
    }

    @Override
    public Integer setDirection(Integer direction) {
        final int newPulseWidth = directionPWMValues.getPulseWidthForPercentage(direction);
        final int oldPulseWidth = arduinoSerialCommandAdapter.setDirection(newPulseWidth);

        return directionPWMValues.getPercentageForPulseWidth(oldPulseWidth);
    }

    @Override
    public Integer getDirection() {
        return directionPWMValues.getPercentageForPulseWidth(arduinoSerialCommandAdapter.getDirection());
    }

    @Override
    public void stop() {
        arduinoSerialCommandAdapter.stop();
    }

}
