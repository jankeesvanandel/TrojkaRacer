package nl.jpoint.trojkaracer.car.domain.finisher;

import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

/**
 * Implementation of the {@link Finisher} interface; it will never signal for the finish, causing the car to keep on driving.
 */
public class NoFinisher implements Finisher {

    private final Flux<Boolean> finishedPublisher;

    public NoFinisher() {
        finishedPublisher = Flux.empty();
    }

    @Override
    public void subscribe(final Subscriber<? super Boolean> s) {
        finishedPublisher.subscribe(s);
    }

}
