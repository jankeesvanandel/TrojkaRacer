package nl.jpoint.trojkaracer.car.infrastructure;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Gpio;
import nl.jpoint.trojkaracer.car.domain.car.CarEngine;
import nl.jpoint.trojkaracer.car.domain.car.Direction;
import nl.jpoint.trojkaracer.car.domain.car.Speed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

/**
 * Implementation of the {@link CarEngine} interface; this implementation controls the car engine through the use of the hardware PWM pins that are
 * onboard of the Raspberry Pi. It uses GPIO_23 pin to control the steering/direction and the GPIO_26 pin to control the motor/speed.
 */
@Component("carEngine")
@Profile("production")
public class PiOnboardPWMCarEngine implements CarEngine {

    private static final int PWM_RANGE = 2000;
    private static final int PWM_CLOCK = 192;
    private static final int PWM_NEUTRAL_SPEED_CONTROLLER = 130;
    private static final int PWM_NEUTRAL_STEERING = 150;
    private static final int PWM_EFFECTIVE_RANGE_SPEED_CONTROLLER = 65;
    private static final int PWM_EFFECTIVE_RANGE_STEERING = 75;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final GpioPinPwmOutput motorPin;
    private final GpioPinPwmOutput steeringPin;

    /**
     * Instantiates and initializes the car engine; it initializes the pins and sets all signals to their neutral positions.
     * @param gpioController the Raspberry Pi GpioController.
     */
    public PiOnboardPWMCarEngine(final GpioController gpioController) {
        LOGGER.info("Creating and initializing a new car engine (carEngine = '{}')", this.getClass().getSimpleName());

        // Initialize the hardware
        motorPin = gpioController.provisionPwmOutputPin(RaspiPin.GPIO_26);
        steeringPin = gpioController.provisionPwmOutputPin(RaspiPin.GPIO_23);
        Gpio.pwmSetMode(Gpio.PWM_MODE_MS);
        Gpio.pwmSetRange(PWM_RANGE);
        Gpio.pwmSetClock(PWM_CLOCK);

        motorPin.setPwm(PWM_NEUTRAL_SPEED_CONTROLLER);
        steeringPin.setPwm(PWM_NEUTRAL_STEERING);
    }

    @Override
    public void updateMotor(final Speed speed) {
        int pwmSpeed = PWM_NEUTRAL_SPEED_CONTROLLER;
        int speedAdjustment = speed.getSpeedAsPercentage() * PWM_EFFECTIVE_RANGE_SPEED_CONTROLLER / 100;

        pwmSpeed += speedAdjustment;
        LOGGER.debug("Setting PWM Speed to {}", pwmSpeed);
        motorPin.setPwm(pwmSpeed);
    }

    @Override
    public void updateDirection(final Direction direction) {
        int pwmDirection = PWM_NEUTRAL_STEERING;
        int directionAdjustment = direction.getDegreesAsPercentage() * PWM_EFFECTIVE_RANGE_STEERING / 100;

        pwmDirection += directionAdjustment;
        LOGGER.debug("Setting PWM Direction to {}", pwmDirection);
        steeringPin.setPwm(pwmDirection);
    }
}
