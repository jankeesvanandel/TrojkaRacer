package nl.jpoint.trojkaracer.car.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

@Getter
public class RaceControlEvent {

    public enum Type {
        MANUAL, INIT_RACE, RACE, STOP, PAUSE, CONTINUE
    }

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    private final int id;
    private RaceControlEvent.Type type;
    private final long timestamp;

    @JsonCreator
    public RaceControlEvent(@JsonProperty("type") RaceControlEvent.Type type) {
        this.id = ID_GENERATOR.addAndGet(1);
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

}
