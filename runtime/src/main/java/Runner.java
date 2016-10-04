import nl.jpoint.trojkaracer.ai.AIService;
import nl.jpoint.trojkaracer.ai.AIServiceImpl;
import nl.jpoint.trojkaracer.pid.PIDController;
import nl.jpoint.trojkaracer.pid.TrojkaRacerPIDController;
import nl.jpoint.trojkaracer.processing.ProcessingService;
import nl.jpoint.trojkaracer.processing.ProcessingServiceImpl;

/**
 * Created by jankeesvanandel on 28/08/16.
 */
public class Runner {
    public static void main(String[] args) {
        ProcessingService processingService = new ProcessingServiceImpl();
        AIService aiService = new AIServiceImpl(processingService);
        PIDController pidController = new TrojkaRacerPIDController(aiService);

        pidController.start();
    }
}
