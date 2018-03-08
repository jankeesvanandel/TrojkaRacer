package nl.jpoint.trojkaracer.car.domain.driver;

import lombok.Value;
import nl.jpoint.trojkaracer.car.domain.car.Car;
import nl.jpoint.trojkaracer.car.domain.car.Direction;
import nl.jpoint.trojkaracer.car.domain.car.Speed;

/**
 * Drive command containing absolute direction and speed values. This command could be used to directly control the car.
 */
@Value(staticConstructor = "of")
public class AbsoluteDriveCommand implements DriveCommand {

    final Speed speed;
    final Direction direction;

    @Override
    public void applyCommandOnCar(final Car car) {
        car.drive(speed, direction);
    }
}
