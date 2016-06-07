public interface ThrottleAction {
    /**
     * @return The desired throttle amount, as a number between -1 and +1.
     * 0 means forced stop.
     * +1 is max throttle forwards.
     * -1 is max throttle reverse.
     */
    float getThrottleAmount();
}
