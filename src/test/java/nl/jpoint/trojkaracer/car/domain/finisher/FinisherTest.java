package nl.jpoint.trojkaracer.car.domain.finisher;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FinisherTest {

    @Test
    public void shouldFinish() {
        final Mono<Void> mono = Mono.empty();

        mono.subscribe(System.out::println,
                t -> System.out.println(t.getMessage()),
                () -> System.out.println("Got Here")
        );
    }

    @Test
    public void shouldFinish2() {
        final Mono<Void> mono = Mono.from(Flux.just("1", "2", "3").doOnNext(s -> System.out.println("- " + s))).then();

        mono.subscribe(System.out::println,
                t -> System.out.println(t.getMessage()),
                () -> System.out.println("Got Here")
        );
    }



    @Test
    public void shouldFinish3() {
//        Flux.just("1", "2", "3", "4").
//        final Flux<Boolean> finishLineDetected = Flux.generate(() -> 0,
//                (state, sink) -> {
//
//                })

        final Mono<Void> mono = Mono.from(Flux.just("1", "2", "3").doOnNext(s -> System.out.println("- " + s))).then();

        mono.subscribe(System.out::println,
                t -> System.out.println(t.getMessage()),
                () -> System.out.println("Got Here")
        );
    }
}
