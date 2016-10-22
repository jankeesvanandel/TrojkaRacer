package nl.jpoint.trojkaracer.hardwareinterface;

/**
 * Interface for the speed controller. This controller provides the throttle/speed on a hardware level.
 */
public interface SpeedController {

    int MIN_SPEED = 0;
    int MAX_SPEED = 100;

    int forward(int amount);

    int backward(int amount);

    int brake();

    int getSpeed();

    int isForward();

    int isBackward();
}
