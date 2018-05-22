package nl.jpoint.trojkaracer.car.domain.driver;

import java.lang.invoke.MethodHandles;
import nl.jpoint.trojkaracer.car.domain.car.Direction;
import nl.jpoint.trojkaracer.car.domain.car.Speed;
import nl.jpoint.trojkaracer.car.domain.navigator.NavigationDirections;
import nl.jpoint.trojkaracer.car.domain.navigator.Navigator;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

/**
 * Simple basic driver implementation of the {@link Driver} interface. It publishes {@link DriveCommand}s based on the navigational directions
 * of the {@link Navigator}.
 */
public class SimpleDriver implements Driver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Flux<DriveCommand> driveCommandFlux;

    public SimpleDriver(final Navigator navigator) {
        LOGGER.info("Creating a new {}.", this.getClass().getSimpleName());

        driveCommandFlux = Flux.from(navigator)
                .map(this::mapNavigationDirectionToDriveCommand);
    }

    /**
     * Converts the received navigation directions into drive commands for the car/.
     * @param navigationDirections the navigation directions to convert.
     * @return the resulting drive command.
     */
    private DriveCommand mapNavigationDirectionToDriveCommand(final NavigationDirections navigationDirections) {
        return AbsoluteDriveCommand.of(new Speed(16), new Direction(navigationDirections.getDegrees()));
    }

    @Override
    public void subscribe(final Subscriber<? super DriveCommand> s) {
        driveCommandFlux.subscribe(s);
    }
}
