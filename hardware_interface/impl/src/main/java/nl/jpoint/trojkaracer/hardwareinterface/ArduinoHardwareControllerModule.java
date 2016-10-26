package nl.jpoint.trojkaracer.hardwareinterface;

import nl.jpoint.trojkaracer.hardwareinterface.adapter.ArduinoSerialCommandAdapter;
import nl.jpoint.trojkaracer.hardwareinterface.adapter.PWMValues;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * The Dagger Dependency Injection Module for the Arduino Hardware Controller; contains the wiring of the object graph.
 */
@Module(library = true)
public class ArduinoHardwareControllerModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArduinoHardwareControllerModule.class);
    private static final String SERIAL_PORT_NAME = "/dev/ttyACM0";

    @Provides
    SerialPort provideSerialPort() {
        final SerialPort serialPort = new SerialPort(SERIAL_PORT_NAME);
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        } catch (final SerialPortException spe) {
            LOGGER.error("Error setting up the Serial Port ({})", SERIAL_PORT_NAME, spe);
            throw new IllegalStateException("Serial port was not set up correctly; without a serial port it simply does not work.", spe);
        }
        return serialPort;
    }

    @Provides
    @Named("speed_pwm")
    PWMValues provideSpeedPWMValues() {
        return new PWMValues(980, 1500, 2000);
    }

    @Provides
    @Named("direction_pwm")
    PWMValues provideDirectionPWMValues() {
        return new PWMValues(982, 1486, 1994);
    }

    @Provides
    ArduinoSerialCommandAdapter provideArduinoSerialCommandAdapter(final SerialPort serialPort,
                                                                   @Named("speed_pwm") final PWMValues speedPWMValues,
                                                                   @Named("direction_pwm") final PWMValues directionPWMValues) {
        return new ArduinoSerialCommandAdapter(serialPort, speedPWMValues, directionPWMValues);
    }

    @Provides
    @Singleton
    ArduinoSerialPercentageController provideArduinoSerialPercentageController(final ArduinoSerialCommandAdapter arduinoSerialCommandAdapter,
                                                                               @Named("speed_pwm") final PWMValues speedPWMValues,
                                                                               @Named("direction_pwm") final PWMValues directionPWMValues) {
        return new ArduinoSerialPercentageController(arduinoSerialCommandAdapter, speedPWMValues, directionPWMValues);
    }

    @Provides
    SpeedController<Integer> provideSpeedController(final ArduinoSerialPercentageController arduinoSerialPercentageController) {
        return arduinoSerialPercentageController;
    }

    @Provides
    DirectionController<Integer> provideDirectionController(final ArduinoSerialPercentageController arduinoSerialPercentageController) {
        return arduinoSerialPercentageController;
    }

}
