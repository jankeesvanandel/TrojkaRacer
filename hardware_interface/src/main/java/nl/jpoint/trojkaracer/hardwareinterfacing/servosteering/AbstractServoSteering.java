package nl.jpoint.trojkaracer.hardwareinterfacing.servosteering;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.util.Console;
import com.pi4j.wiringpi.Gpio;

/**
 * Abstract base implementation of the {@link ServoSteering} interface meant for steering servo motors. Provides default implementations of the methods
 * for steering in a certain direction or center the steer.
 * <p>
 *     This base class also provides a constructor for constructing an instance based on its hardware settings; its required pulse width
 *     for left, center and right (and its total pulse width). Note that these are all expected to be provided in microseconds.
 * </p>
 */
abstract class AbstractServoSteering implements ServoSteering {

    private static final int MICROS = 1000000;
    private static final int DEFAULT_RANGE = 1000;
    private static final int DEFAULT_TOTAL_PULSE_WIDTH = 20000;

    private final GpioPinPwmOutput pwmPin;
    private final Console console = new Console();

    private final int clock;
    private final int range;
    private final int minimalPWMSignal;
    private final int centerPWMSignal;
    private final int maximalPWMSignal;

    AbstractServoSteering(final GpioController gpioController,
                          final int lowPulseWidth,
                          final int highPulseWidth) {
        this(gpioController, lowPulseWidth, (highPulseWidth + lowPulseWidth) / 2, highPulseWidth, DEFAULT_TOTAL_PULSE_WIDTH);
    }

    AbstractServoSteering(final GpioController gpioController,
                          final int lowPulseWidth,
                          final int centerPulseWidth,
                          final int highPulseWidth,
                          final int totalPulseWidth) {
        this(gpioController, lowPulseWidth, centerPulseWidth, highPulseWidth, totalPulseWidth, DEFAULT_RANGE);
    }

    AbstractServoSteering(final GpioController gpioController,
                          final int lowPulseWidth,
                          final int centerPulseWidth,
                          final int highPulseWidth,
                          final int totalPulseWidth,
                          final int range) {
        this(gpioController, lowPulseWidth, centerPulseWidth, highPulseWidth, totalPulseWidth, range, RaspiPin.GPIO_01);
    }

    AbstractServoSteering(final GpioController gpioController,
                          final int lowPulseWidth,
                          final int centerPulseWidth,
                          final int highPulseWidth,
                          final int totalPulseWidth,
                          final int range,
                          final Pin pin) {
        final double pulseFrequency = MICROS / totalPulseWidth;
        this.clock = Double.valueOf(DEFAULT_CLOCK_FREQUENCY / (range * pulseFrequency)).intValue();
        this.range = range;

        this.minimalPWMSignal = range * lowPulseWidth / totalPulseWidth;
        this.centerPWMSignal = range * centerPulseWidth / totalPulseWidth;
        this.maximalPWMSignal = range * highPulseWidth / totalPulseWidth;

        // print program title/header
        console.title("<-- The Pi4J Project -->", "PWM Example");

        // allow for user to exit program using CTRL-C
        console.promptForExit();

        pwmPin = gpioController.provisionPwmOutputPin(pin);

        Gpio.pwmSetMode(Gpio.PWM_MODE_MS);
        Gpio.pwmSetRange(range);
        Gpio.pwmSetClock(clock);

//        pwmPin.setShutdownOptions(true);
//        pwmPin.setPwm(centerPWMSignal);

        console.println("Clock: " + clock);
        console.println("Range: " + range);
        console.println("MinimalPWMSignal: " + minimalPWMSignal);
        console.println("MaximalPWMSignal: " + maximalPWMSignal);
        console.println("CenterPWMSignal: " + centerPWMSignal);
    }

    @Override
    public void center() {
        this.steer(CENTER_STEERING_FACTOR);
    }

    @Override
    public void steer(int direction) {
        if (direction < MIN_STEERING_FACTOR || direction > MAX_STEERING_FACTOR) {
            throw new IllegalArgumentException(String.format("Steering direction was not in the expected range. It should be between %s and %s, but was %s.",
                    MIN_STEERING_FACTOR, MAX_STEERING_FACTOR, direction));
        }

        final double steeringFactor = (1.0 * direction / (MAX_STEERING_FACTOR - MIN_STEERING_FACTOR));
        console.println("   SteeringFactor: " + steeringFactor);
        final double steeringDirection = steeringFactor * (maximalPWMSignal - minimalPWMSignal) + minimalPWMSignal;
        console.println("   steeringDirection: " + steeringDirection);
        console.println("Setting pwm to " + Double.valueOf(steeringDirection).intValue());
        pwmPin.setPwm(Double.valueOf(steeringDirection).intValue());
    }

    /**
     * Returns the PWM range that is used.
     * @return the PWM range that is used.
     */
    public int getRange() {
        return range;
    }

    /**
     * Returns the PWM clock that is used.
     * @return the PWM clock that is used.
     */
    public int getClock() {
        return clock;
    }

    /**
     * Returns the size of the minimal PWM Signal value; the value to steer fully left.
     * @return the size of the minimal PWM Signal value.
     */
    public int getMinimalPWMSignal() {
        return minimalPWMSignal;
    }

    /**
     * Returns the size of the center PWM Signal value; the value to steer to the center.
     * @return the size of the center PWM Signal value.
     */
    public int getCenterPWMSignal() {
        return centerPWMSignal;
    }

    /**
     * Returns the size of the maximal PWM Signal value; the value to steer fully right.
     * @return the size of the maximal PWM Signal value.
     */
    public int getMaximalPWMSignal() {
        return maximalPWMSignal;
    }

}
