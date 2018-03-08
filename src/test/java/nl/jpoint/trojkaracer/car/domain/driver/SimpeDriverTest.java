package nl.jpoint.trojkaracer.car.domain.driver;

import nl.jpoint.trojkaracer.car.domain.navigator.FixedPathNavigator;
import org.junit.Ignore;
import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.UnicastProcessor;

/**
 * Unit tests for the {@link SimpleDriver} class.
 */
public class SimpeDriverTest {

    @Test
    @Ignore
    public void shouldDrive() throws InterruptedException {
        final SimpleDriver simpleDriver = new SimpleDriver(new FixedPathNavigator());
        simpleDriver.subscribe(new BaseSubscriber<DriveCommand>() {
            @Override
            protected void hookOnNext(DriveCommand value) {
                super.hookOnNext(value);
                System.out.println("Recieved DriveCommand: " + value);
            }
        });

        Thread.sleep(2000);
    }

    @Test
    public void shouldReSubscribe() {
        final UnicastProcessor<String> unicastProcessor = UnicastProcessor. create();
        final DirectProcessor<String> directProcessor = DirectProcessor.create();

        final BaseSubscriber<String> baseSubscriber = buildBaseSubscriber();

        unicastProcessor.subscribe(directProcessor);

        unicastProcessor.onNext("test 1");
        unicastProcessor.onNext("test 2");

        directProcessor.subscribe(baseSubscriber);

        unicastProcessor.onNext("test 3");
        unicastProcessor.onNext("test 4");
        unicastProcessor.onNext("test 5");

        baseSubscriber.cancel();

        unicastProcessor.onNext("test 6");
        unicastProcessor.onNext("test 7");
        unicastProcessor.onNext("test 8");

        directProcessor.subscribe(buildBaseSubscriber());

        unicastProcessor.onNext("test 9");
        unicastProcessor.onNext("test 10");
        unicastProcessor.onNext("test 11");

    }

    Subscription subscription1 = null;

    private BaseSubscriber<String> buildBaseSubscriber() {
        return new BaseSubscriber<String>() {
            @Override
            protected void hookOnSubscribe(final Subscription subscription) {
                super.hookOnSubscribe(subscription);
                System.out.println("Subscribing...");

                subscription1 = subscription;
            }

            @Override
            protected void hookOnNext(String value) {
                super.hookOnNext(value);
                System.out.println("New value: " + value);
            }

            @Override
            protected void hookFinally(SignalType type) {
                super.hookFinally(type);
                System.out.println("Finally of type: " + type);
            }
        };
    }
}
