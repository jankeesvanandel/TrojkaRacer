package nl.jpoint.trojkaracer.pid;

import nl.jpoint.trojkaracer.ai.AIService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * The main PID Controller for the TrojkaRacer. It controls the
 */
@Singleton
public class TrojkaRacerPIDController implements PIDController {

    private final AIService aiService;

    private boolean running;


    @Inject
    public TrojkaRacerPIDController(final AIService aiService) {
        this.aiService = aiService;
        running = false;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void start() {
        initialize();
        run();
    }

    @Override
    public void stop() {

    }

    @Override
    public void kill() {
        this.running = false;
    }

    private void run() {
        running = true;
        while (running) {
        }
        running = false;
    }

}
