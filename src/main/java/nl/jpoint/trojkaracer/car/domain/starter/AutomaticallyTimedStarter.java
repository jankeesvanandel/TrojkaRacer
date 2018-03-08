package nl.jpoint.trojkaracer.car.domain.starter;

import java.time.Duration;
import nl.jpoint.trojkaracer.car.domain.finisher.Finisher;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementation of the {@link Starter} interface; it will automatically signal the car start every 2 seconds.
 */
public class AutomaticallyTimedStarter implements Starter {

    private final Publisher<Boolean> startPublisher;

    public AutomaticallyTimedStarter() {
        startPublisher = Flux.interval(Duration.ofSeconds(2))
                .map(l -> Boolean.TRUE);
    }

    @Override
    public void subscribe(final Subscriber<? super Boolean> s) {
        startPublisher.subscribe(s);
    }
}
