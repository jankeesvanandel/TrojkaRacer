package nl.jpoint.trojkaracer.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link AIService} implementation that actually does no AI at all, but simply responds with desired actions taken from a sequential list of desired actions.
 * This implementation can be used for testing purposes or hardcoding the direction and speed the car should get in sequential order.
 */
public class HardWiredAIService implements AIService {

    private static final int MAX_LOOP = 10;

    private static final Logger LOGGER = LoggerFactory.getLogger(HardWiredAIService.class);

    private final List<DesiredActions> desiredActionsList;
    private int counter;
    private int loopCounter;

    public HardWiredAIService() {
        desiredActionsList = new ArrayList<>();
        counter = 0;
        loopCounter = 0;
        desiredActionsList.add(buildDesiredActions(0, 0.0));
        desiredActionsList.add(buildDesiredActions(0.195, 0.0));
        desiredActionsList.add(buildDesiredActions(0, 0.0));
    }

    @Override
    public DesiredActions getDesiredActions() {
        if (counter >= desiredActionsList.size()) {
            LOGGER.info("Hardwaired AI Server is finished....no desired actions anymore");
            return buildDesiredActions(0, 0);
        } else {
            DesiredActions desiredActions = desiredActionsList.get(counter);
            if (loopCounter++ >= MAX_LOOP) {
                loopCounter = 0;
                counter++;
            }
            return desiredActions;
        }
    }

    private DesiredActions buildDesiredActions(final double speed, final double direction) {
        return new DesiredActions(new SteeringAction(direction), new ThrottleAction(speed));
    }
}
