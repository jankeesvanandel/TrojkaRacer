import java.util.Optional;

public interface DesiredActions {
    Optional<SteeringAction> getSteeringAction();
    Optional<ThrottleAction> getThrottleAction();
}
