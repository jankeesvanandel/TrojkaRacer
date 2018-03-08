package nl.jpoint.trojkaracer.car.domain.finisher;

import org.reactivestreams.Publisher;

/**
 * Interface for the finisher, the object that checks if the car has passed the finish line and then sends a (single) signal.
 */
public interface Finisher extends Publisher<Boolean> {

}
