package nl.jpoint.trojkaracer.car.domain.navigator;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

/**
 * Implementation of the {@link Navigator} interface; sends a navigation direction every quarter of a second, where the navigation directions
 * are hard coded in this class.
 */
public class FixedPathNavigator implements Navigator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Flux<NavigationDirections> fixedNavigationFlux;

    public FixedPathNavigator() {
        fixedNavigationFlux = Flux.interval(Duration.ofMillis(250))
                .zipWith(Flux.just(0, 5, 15, 25, 25, 22, 15, 5, -15, -5, 0).repeat(),
                        (l, i) -> NavigationDirections.of(i))
                .doOnNext(navDir -> LOG.debug("Sending navigation direction {}", navDir));
    }

    @Override
    public void subscribe(Subscriber<? super NavigationDirections> s) {
        fixedNavigationFlux.subscribe(s);
    }
}
