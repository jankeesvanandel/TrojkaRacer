package nl.jpoint.trojkaracer.pid;

/**
 * This class represents a PID Controller that can be started (and  can be killed/stopped).
 */
public interface PIDController extends Killable {

    void initialize();

    void start();

    void stop();

}
