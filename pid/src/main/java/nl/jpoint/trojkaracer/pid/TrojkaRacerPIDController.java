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
        // TODO: Waarom hebben we de logica van het loopen e.d. in de controllers?
        // Kunnen we niet beter een losse module maken die bepaalt of dingen moeten loopen en de controllers met 'business logica' een methode geven die constant wordt aangeroepen?
        // Op die manier scheiden we het loopen van de logica in de controller.
        running = true;
        while (running) {
        }
        running = false;
    }

}
