package nl.jpoint.trojkaracer.car.domain.starter;

import org.reactivestreams.Publisher;

/**
 * Interface for the starter, the object that checks if the car can start and sends a (single) signal when the car can start the race.
 */
public interface Starter extends Publisher<Boolean> {

}
