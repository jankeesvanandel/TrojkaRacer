package nl.jpoint.trojkaracer.hardwareinterface.impl;

import nl.jpoint.trojkaracer.hardwareinterface.SpeedController;
import nl.jpoint.trojkaracer.hardwareinterface.SteeringController;

import javax.inject.Inject;
import javax.inject.Singleton;

import jssc.SerialPort;
import jssc.SerialPortException;


/**
 * Created by tim on 16-10-16.
 */
@Singleton
public class ArduinoSerialServoController implements SpeedController, SteeringController {

    private static final String CMD_SPEED = "THR";
    private static final String CMD_DIRECTION = "STE";

    private final SerialPort serialPort;
    private final int servoMinPulseWidth;
    private final int servoMaxPulseWidth;

    public ArduinoSerialServoController(final SerialPort serialPort) {
        this(serialPort, 1000, 2000);
    }

    @Inject
    public ArduinoSerialServoController(final SerialPort serialPort, final int servoMinPulseWidth, final int servoMaxPulseWidth) {
        this.serialPort = serialPort;
        this.servoMinPulseWidth = servoMinPulseWidth;
        this.servoMaxPulseWidth = servoMaxPulseWidth;
    }

    @Override
    public int forward(int amount) {
        return sendCommand(CMD_SPEED, amount);
    }

    @Override
    public int backward(int amount) {
        return sendCommand(CMD_SPEED, amount);
    }

    @Override
    public int brake() {
        return sendCommand(CMD_SPEED, 1500);
    }

    @Override
    public int getSpeed() {
        return 0;
    }

    @Override
    public int isForward() {
        return 0;
    }

    @Override
    public int isBackward() {
        return 0;
    }

    @Override
    public int steerDegrees(int degrees) {
        if (degrees < 0 || degrees > 180) {
            throw new IllegalArgumentException();
        }

        final int value = ((degrees * 1000) / 180) + 1000;
        return sendCommand(CMD_DIRECTION, value);
    }

    private int sendCommand(final String cmd, final int value) {
        try {
//            System.out.println("Got this before writing: " + serialPort.readString());

            final String msg = String.format("%s%s\n", cmd, value);
            System.out.print(msg);
            serialPort.writeString(msg);
            System.out.println("Sent....");
            System.out.println("Return: " + serialPort.readString());
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getSteering() {
        return 0;
    }

    public class ArduinoCommandBuilder {

        private String cmd;
        private int value;

        private ArduinoCommandBuilder() {
            // Do nothing
        }

        public ArduinoCommandBuilder build() {
            return new ArduinoCommandBuilder();
        }

        public ArduinoCommandBuilder withCommand(final String str) {
            this.cmd = str;
            return this;
        }

        public ArduinoCommandBuilder withValue(final int value) {
            this.value = value;
            return this;
        }
    }
}
