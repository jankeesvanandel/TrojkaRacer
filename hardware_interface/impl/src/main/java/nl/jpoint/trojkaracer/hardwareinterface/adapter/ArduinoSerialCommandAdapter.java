package nl.jpoint.trojkaracer.hardwareinterface.adapter;

import nl.jpoint.trojkaracer.hardwareinterface.HardwareInterfaceException;
import nl.jpoint.trojkaracer.hardwareinterface.PWMPulseOutsideLimitsException;
import nl.jpoint.trojkaracer.hardwareinterface.Stoppable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * Arduino adapter that sends String commands over the Serial (USB) line to both set speed and direction. The speed and direction are send as pulse widths
 * in microseconds.
 * <p>
 *     This adapter is compatible with both the <tt>arduino_serial_servo_control</tt> and <tt>arduino_serial_servo_control_buffered</tt> scripts.
 * </p>
 */
public class ArduinoSerialCommandAdapter implements Stoppable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArduinoSerialCommandAdapter.class);

    private static final String SERIAL_COMMAND_BREAK = "\n";
    private static final String SPEED_COMPONENT_NAME = "SpeedController";
    private static final String DIRECTION_COMPONENT_NAME = "DirectionController";

    private final SerialPort serialPort;
    private final PWMValues speedPWMValues;
    private final PWMValues directionPWMValues;

    /**
     * Default constructor that sets the {@link SerialPort} to use for communication and the two (possibly) different {@link PWMValues} that define the two
     * controlled hardware components.
     *
     * @param serialPort the serial port to use to communicate to the Arduino.
     * @param speedPWMValues the PWM values, containing the min, max and neutral pulse widths for the Speed Servo.
     * @param directionPWMValues the PWM values, containing the min, max and neutral pulse widths for the Direction Servo.
     */
    @Inject
    public ArduinoSerialCommandAdapter(final SerialPort serialPort, final PWMValues speedPWMValues, final PWMValues directionPWMValues) {
        this.serialPort = serialPort;
        this.speedPWMValues = speedPWMValues;
        this.directionPWMValues = directionPWMValues;

        LOGGER.info("Created {} instance with serial port set to {}", this.getClass().getSimpleName(), serialPort.getPortName());
    }

    /**
     * Sets the speed to the provided pulse width.
     * @param pulseWidth the pulse width to set the speed.
     * @return the speed (as a pulse width in microseconds) that was used before setting the new speed.
     */
    public int setSpeed(final int pulseWidth) {
        checkForIncorrectPulseWidth(speedPWMValues, pulseWidth, SPEED_COMPONENT_NAME);

        final CommandResult result = sendCommand(Command.SPEED, pulseWidth);
        return result.getSpeed();
    }

    /**
     * Returns the current speed.
     * @return the current speed.
     */
    public int getSpeed() {
        return 0;
    }

    /**
     * Sets the direction to the provided pulse width.
     * @param pulseWidth the pulse width to set the direction.
     * @return the direction (as a pulse width in microseconds) that was used before setting the new direction.
     */
    public int setDirection(final int pulseWidth) {
        checkForIncorrectPulseWidth(directionPWMValues, pulseWidth, DIRECTION_COMPONENT_NAME);

        final CommandResult result = sendCommand(Command.DIRECTION, pulseWidth);
        return result.getDirection();
    }

    /**
     * Returns the current direction.
     * @return the current direction.
     */
    public int getDirection() {
        return 0;
    }

    @Override
    public void stop() {
        setSpeed(speedPWMValues.getNeutralPulseWidth());
        setDirection(directionPWMValues.getNeutralPulseWidth());
    }

    /**
     * Checks if the provided pulse width is not outside the minimum or maximum PWM values. If the pulse width is outside the limits, an exception is thrown.
     * @param pwmValues the WPM specific values that contain the lower and higher limits that are allowed.
     * @param pulseWidth the width of the pulse (in microseconds).
     */
    private void checkForIncorrectPulseWidth(final PWMValues pwmValues, final int pulseWidth, final String componentName) {
        if (pwmValues.isOutsideLimits(pulseWidth)) {
            throw new PWMPulseOutsideLimitsException(componentName, pulseWidth, pwmValues.getMinimumPulseWidth(), pwmValues.getMaximumPulseWidth());
        }
    }

    /**
     * Sends the provided command and value to the Arduino over the Serial (USB) line.
     * @param cmd the command to send.
     * @param value the value to provide as argument to the command.
     * @return a {@link CommandResult} representing the result returned from the Arduino.
     */
    private CommandResult sendCommand(final Command cmd, final int value) {
        final String msg = String.format("%s%s", cmd, value);
        LOGGER.debug("Sending message '{}' to the Arduino", msg);
        try {
            serialPort.writeString(msg + SERIAL_COMMAND_BREAK);

            // TODO: Retrieve the old speed and direction and return these values
        } catch (final SerialPortException spe) {
            LOGGER.error("Error during sending of command '{}' to the Arduino over the serial port.", msg, spe);
            throw new HardwareInterfaceException(spe);
        }
        return new CommandResult("0;0");
    }

    /**
     * Internal enumeration of all possible commands to send to the Arduino. The string representation is the actual command to send.
     */
    private enum Command {
        SPEED("THR"),
        DIRECTION("STE");

        private final String cmd;

        Command(final String cmd) {
            this.cmd = cmd;
        }

        @Override
        public String toString() {
            return cmd;
        }
    }

    /**
     * Inner class representing the result of the command sent to the Arduino. This result object contains the speed and direction
     */
    private class CommandResult {

        private static final String COMMAND_RESULT_SEPARATOR = ";";

        private final int speed;
        private final int direction;

        private CommandResult(final String commandResult) {
            if (commandResult == null || !commandResult.contains(COMMAND_RESULT_SEPARATOR)) {
                speed = 0;
                direction = 0;
            } else {
                final String[] values = commandResult.split(COMMAND_RESULT_SEPARATOR);
                speed = Integer.parseInt(values[0]);
                direction = Integer.parseInt(values[1]);
            }
        }

        private int getSpeed() {
            return speed;
        }

        private int getDirection() {
            return direction;
        }
    }

}
