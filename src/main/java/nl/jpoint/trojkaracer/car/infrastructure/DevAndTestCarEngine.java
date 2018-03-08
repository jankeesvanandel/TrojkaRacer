package nl.jpoint.trojkaracer.car.infrastructure;

import nl.jpoint.trojkaracer.car.domain.car.CarEngine;
import nl.jpoint.trojkaracer.car.domain.car.Speed;
import nl.jpoint.trojkaracer.car.domain.car.Direction;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import lombok.Getter;

/**
 * Implementation of the {@link CarEngine} interface, specifically meant for testing and for when running the application in
 * development mode (i.e. when not run on the Raspberry Pi).
 */
@Component("carEngine")
@Profile({"development", "development-laptop"})
@Getter
public class DevAndTestCarEngine implements CarEngine {

    private Speed speed;
    private Direction direction;

    @Override
    public void updateMotor(final Speed speed) {
        this.speed = speed;
    }

    @Override
    public void updateDirection(final Direction direction) {
        this.direction = direction;
    }

}
