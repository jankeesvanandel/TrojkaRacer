package nl.jpoint.trojkaracer.car.domain.driver;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public class WrappingDriver implements Driver {
    private final Publisher<DriveCommand> driveCommandPublisher;

    public WrappingDriver(final Publisher<DriveCommand> driveCommandPublisher) {
        this.driveCommandPublisher = driveCommandPublisher;
    }

    @Override
    public void subscribe(Subscriber<? super DriveCommand> s) {
        driveCommandPublisher.subscribe(s);
    }
}
