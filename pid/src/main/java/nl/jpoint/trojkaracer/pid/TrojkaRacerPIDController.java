package nl.jpoint.trojkaracer.pid;

import javax.inject.Singleton;

/**
 * The main PID Controller for the TrojkaRacer. It controls the
 */
@Singleton
public class TrojkaRacerPIDController implements PIDController {

    private final PID steeringPid;
    private final PID throttlePid;

    public TrojkaRacerPIDController() {
        this.steeringPid = new PID(6, 0.6, 1, -1.0, 1.0);
        this.throttlePid = new PID(0, 0, 0, -1.0, 1.0);
    }

    @Override
    public double calculateSteeringAndThrottle(double requestedSteering, boolean requestedThrottle) {
        return this.steeringPid.calculate(requestedSteering, 0.0);
    }

    public static void main(String[] args) throws InterruptedException {
        PID steeringPid = new PID(0.5, 2.0, 0, -1.0, 1.0);
        steeringPid.setDirection(PID.Direction.DIRECT);
        steeringPid.setMode(PID.Mode.AUTOMATIC);

        double input = 0;
        for (int i = 0; i < 100; i++) {
            double calculated = steeringPid.calculate(input, 0.8);
            System.out.println("input = " + input + ", output = " + calculated);
            input = calculated;
            Thread.sleep(50L);

        }
    }
}