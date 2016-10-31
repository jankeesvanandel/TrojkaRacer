package nl.jpoint.trojkaracer.hardwareinterface.adapter;

import nl.jpoint.trojkaracer.hardwareinterface.HardwareInterfaceException;
import nl.jpoint.trojkaracer.hardwareinterface.PWMPulseOutsideLimitsException;
import nl.jpoint.trojkaracer.hardwareinterface.Stoppable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

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

    private boolean isStopped;

    /**
     * Default constructor that sets the {@link SerialPort} to use for communication and the two (possibly) different {@link PWMValues} that define the two
     * controlled hardware components.
     *
     * @param serialPort the serial port to use to communicate to the Arduino.
     * @param speedPWMValues the PWM values, containing the min, max and neutral pulse widths for the Speed Servo.
     * @param directionPWMValues the PWM values, containing the min, max and neutral pulse widths for the Direction Servo.
     */
    @Inject
    public ArduinoSerialCommandAdapter(final SerialPort serialPort,
                                       @Named("speed_pwm") final PWMValues speedPWMValues,
                                       @Named("direction_pwm") final PWMValues directionPWMValues) {
        this.serialPort = serialPort;
        this.speedPWMValues = speedPWMValues;
        this.directionPWMValues = directionPWMValues;

        isStopped = false;
        sendCommand(Command.INITIALIZATION, 10);
        LOGGER.info("Created {} instance with serial port set to {}", this.getClass().getSimpleName(), serialPort.getPortName());
    }

    /**
     * Sets the speed to the provided pulse width.
     * @param pulseWidth the pulse width to set the speed.
     * @return the speed (as a pulse width in microseconds) that was used before setting the new speed.
     */
    public int setSpeed(final int pulseWidth) {
        final CommandResult result;
        if (isStopped) {
            LOGGER.debug("Did not set speed to new value {} as system is stopped.", pulseWidth);
            result = sendCommand(Command.GET_INFO, 0);
        } else {
            checkForIncorrectPulseWidth(speedPWMValues, pulseWidth, SPEED_COMPONENT_NAME);
            result = sendCommand(Command.SPEED, pulseWidth);
        }
        return result.getSpeed();
    }

    /**
     * Returns the current speed.
     * @return the current speed.
     */
    public int getSpeed() {
        final CommandResult result = sendCommand(Command.GET_INFO, 0);
        return result.getSpeed();
    }

    /**
     * Sets the direction to the provided pulse width.
     * @param pulseWidth the pulse width to set the direction.
     * @return the direction (as a pulse width in microseconds) that was used before setting the new direction.
     */
    public int setDirection(final int pulseWidth) {
        final CommandResult result;
        if (isStopped) {
            LOGGER.debug("Did not set direction to new value {} as system is stopped.", pulseWidth);
            result = sendCommand(Command.GET_INFO, 0);
        } else {
            checkForIncorrectPulseWidth(directionPWMValues, pulseWidth, DIRECTION_COMPONENT_NAME);
            result = sendCommand(Command.DIRECTION, pulseWidth);
        }
        return result.getDirection();
    }

    /**
     * Returns the current direction.
     * @return the current direction.
     */
    public int getDirection() {
        final CommandResult result = sendCommand(Command.GET_INFO, 0);
        return result.getDirection();
    }

    @Override
    public void stop() {
        isStopped = true;
        sendCommand(Command.SPEED, speedPWMValues.getNeutralPulseWidth());
        sendCommand(Command.DIRECTION, directionPWMValues.getNeutralPulseWidth());

        LOGGER.debug("Stopped with pulse width for speed of {} and for direction of {}", speedPWMValues.getNeutralPulseWidth(),
                directionPWMValues.getNeutralPulseWidth());
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

        final CommandResult commandResult;
        try {
            serialPort.writeString(msg + SERIAL_COMMAND_BREAK);

            final String commandResultMsg = readTillReceived().trim();
            if (commandResultMsg.contains("\n")) {
                LOGGER.info("Received the result of two commands at once....only returning the last command");
                final String[] commandResultMessages = commandResultMsg.split("\n");
                commandResult = new CommandResult((commandResultMessages[commandResultMessages.length - 1]));
            } else {
                commandResult = new CommandResult(commandResultMsg);
            }
        } catch (final SerialPortException spe) {
            LOGGER.error("Error during sending of command '{}' to the Arduino over the serial port (or during reading of its result).", msg, spe);
            throw new HardwareInterfaceException(spe);
        }
        return commandResult;
    }

    private String readTillReceived() throws SerialPortException {
        final StringBuilder result = new StringBuilder();

        // Procedure to wait till init message is fully retrieved.
        try {
            String messageReceived = null;
            while (messageReceived == null || !messageReceived.endsWith(SERIAL_COMMAND_BREAK)) {
                Thread.sleep(10);
                messageReceived = serialPort.readString();
                if (messageReceived != null) {
                    result.append(messageReceived);
                }
            }
        } catch (final InterruptedException ie) {
            LOGGER.error("Caught interruption when reading serial port. Resetting interrupt on thread (as exception is not rethrown).");
            // Restore the interrupted status
            Thread.currentThread().interrupt();
        }

        LOGGER.debug("Read from serial port: {}", result.toString());
        return result.toString();
    }

    /**
     * Internal enumeration of all possible commands to send to the Arduino. The string representation is the actual command to send.
     */
    private enum Command {
        SPEED("THR"),
        DIRECTION("STE"),
        INITIALIZATION("INI"),
        GET_INFO("GET");

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
