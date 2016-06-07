package nl.jpoint.trojkaracer;

public interface SteeringAction {
    /**
     * @return The desired steering position, as a number between -1 and +1, with 0 being exactly centered.
     * -1 is max right, +1 is max left.
     */
    float getSteeringPosition();
}
