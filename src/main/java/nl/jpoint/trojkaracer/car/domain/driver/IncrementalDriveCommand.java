package nl.jpoint.trojkaracer.car.domain.driver;

import lombok.Value;
import nl.jpoint.trojkaracer.car.domain.car.Car;

/**
 * Drive command containing incremental driving direction. This command could be used to control the car by increasing or decreasing speed or
 * steering direction.
 */
@Value(staticConstructor = "of")
public class IncrementalDriveCommand implements DriveCommand {

    private final DriveDirection driveDirection;

    @Override
    public void applyCommandOnCar(final Car car) {
        switch (driveDirection) {
            case FORWARD:
                car.increaseSpeed();
                break;
            case BACKWARD:
                car.decreaseSpeed();
                break;
            case LEFT:
                car.steerLeft();
                break;
            case RIGHT:
                car.steerRight();
                break;
            case STOP:
            default:
                car.stop();
        }
    }

    public enum DriveDirection {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        STOP
    }
}
