package nl.jpoint.trojkaracer.car.domain.driver;

import nl.jpoint.trojkaracer.car.domain.car.Car;

/**
 * Command to drive the car. This command can be applied to the car, driving the car to a certain speed and direction.
 */
public interface DriveCommand {

    /**
     * Applies this command to the provided {@link Car}.
     * @param car the car to apply the command to.
     */
    void applyCommandOnCar(Car car);
}
