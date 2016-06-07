package nl.jpoint.trojkaracer.ai;

public final class DesiredActions {

    private final SteeringAction steeringAction;
    private final ThrottleAction throttleAction;

    public DesiredActions(final SteeringAction steeringAction, final ThrottleAction throttleAction) {
        this.steeringAction = steeringAction;
        this.throttleAction = throttleAction;
    }

    public SteeringAction getSteeringAction() {
        return steeringAction;
    }

    public ThrottleAction getThrottleAction() {
        return throttleAction;
    }
}
