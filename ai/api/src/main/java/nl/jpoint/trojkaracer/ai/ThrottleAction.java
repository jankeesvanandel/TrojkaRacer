package nl.jpoint.trojkaracer.ai;

public final class ThrottleAction {

    private final double throttleAmount;

    public ThrottleAction(final double throttleAmount) {
        this.throttleAmount = throttleAmount;
    }

    /**
     * @return The desired throttle amount, as a number between -1 and +1.
     * 0 means forced stop.
     * +1 is max throttle forwards.
     * -1 is max throttle reverse.
     */
    public double getThrottleAmount() {
        return throttleAmount;
    }
}
