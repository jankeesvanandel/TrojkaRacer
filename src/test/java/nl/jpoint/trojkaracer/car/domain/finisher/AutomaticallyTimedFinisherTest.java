package nl.jpoint.trojkaracer.car.domain.finisher;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

/**
 * Unit tests for the {@link AutomaticallyTimedFinisher} class.
 */
public class AutomaticallyTimedFinisherTest {

    @Test
    public void shouldAutomaticallyFinish() throws InterruptedException {
        final AutomaticallyTimedFinisher automaticallyTimedFinisher = new AutomaticallyTimedFinisher();
        automaticallyTimedFinisher.subscribe(new BaseSubscriber<Boolean>() {
            @Override
            public void hookOnSubscribe(Subscription s) {
                super.hookOnSubscribe(s);
                System.out.println("test 1");
            }

            @Override
            public void hookOnNext(Boolean aBoolean) {
                super.hookOnNext(aBoolean);
                System.out.println("test 2");
            }

            @Override
            public void hookOnError(Throwable t) {
                super.hookOnError(t);
                System.out.println("test 3: " + t.getMessage());
            }

            @Override
            public void hookOnComplete() {
                super.hookOnComplete();
                System.out.println("test 4");
            }
        });
        Thread.sleep(10000l);
    }
}
