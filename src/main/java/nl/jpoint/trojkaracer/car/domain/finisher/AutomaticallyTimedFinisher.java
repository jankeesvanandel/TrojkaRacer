package nl.jpoint.trojkaracer.car.domain.finisher;

import java.time.Duration;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

/**
 * Implementation of the {@link Finisher} interface; it will automatically signal the car is finished every (configurable) number of seconds.
 */
public class AutomaticallyTimedFinisher implements Finisher {

    private static final Duration DEFAULT_FINISH_DURATION = Duration.ofSeconds(5);

    private final Flux<Boolean> finishedPublisher;

    public AutomaticallyTimedFinisher() {
        this(DEFAULT_FINISH_DURATION);
    }

    public AutomaticallyTimedFinisher(final Duration duration) {
        finishedPublisher = Flux.interval(duration)
                .map(l -> Boolean.TRUE);
    }

    @Override
    public void subscribe(final Subscriber<? super Boolean> s) {
        finishedPublisher.subscribe(s);
    }
}
