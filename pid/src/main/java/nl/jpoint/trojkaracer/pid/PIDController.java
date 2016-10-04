package nl.jpoint.trojkaracer.pid;

/**
 * This class represents a PID Controller that can be started (and  can be killed/stopped).
 */
public interface PIDController {

    double calculateSteeringAndThrottle(double requestedSteering, boolean requestedThrottle);

}
