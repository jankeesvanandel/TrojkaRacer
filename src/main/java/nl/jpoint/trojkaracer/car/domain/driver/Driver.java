package nl.jpoint.trojkaracer.car.domain.driver;

import org.reactivestreams.Publisher;

/**
 * Interface for the driver, where a driver publishes drive commands which could be used to directly control the car.
 */
public interface Driver extends Publisher<DriveCommand> {

}
