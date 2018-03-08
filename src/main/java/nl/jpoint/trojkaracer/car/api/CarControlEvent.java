package nl.jpoint.trojkaracer.car.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import nl.jpoint.trojkaracer.car.domain.driver.DriveCommand;
import nl.jpoint.trojkaracer.car.domain.driver.IncrementalDriveCommand;

@Getter
public class CarControlEvent {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);
    private static final int SPEED_STEP = 5;
    private static final int DIRECTION_STEP = 5;

    private final int id;
    private Type type;
    private final long timestamp;

    @JsonCreator
    public CarControlEvent(@JsonProperty("type") Type type) {
        this.id = ID_GENERATOR.addAndGet(1);
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public DriveCommand toDriveCommand() {
        final DriveCommand driveCommand;

        switch (type) {
            case FORWARD:
                driveCommand = IncrementalDriveCommand.of(IncrementalDriveCommand.DriveDirection.FORWARD);
                break;
            case BACKWARD:
                driveCommand = IncrementalDriveCommand.of(IncrementalDriveCommand.DriveDirection.BACKWARD);
                break;
            case LEFT:
                driveCommand = IncrementalDriveCommand.of(IncrementalDriveCommand.DriveDirection.LEFT);
                break;
            case RIGHT:
                driveCommand = IncrementalDriveCommand.of(IncrementalDriveCommand.DriveDirection.RIGHT);
                break;
            case STOP:
                driveCommand = IncrementalDriveCommand.of(IncrementalDriveCommand.DriveDirection.STOP);
                break;
            default:
                driveCommand = IncrementalDriveCommand.of(IncrementalDriveCommand.DriveDirection.STOP);
        }

        return driveCommand;
    }

    public enum Type {
        FORWARD, BACKWARD, LEFT, RIGHT, STOP
    }
}
