package nl.jpoint.trojkaracer.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tim on 28-10-16.
 */
public class HardWiredAIService implements AIService {

    private static final int MAX_LOOP = 20;

    private static final Logger LOGGER = LoggerFactory.getLogger(HardWiredAIService.class);

    private final List<DesiredActions> desiredActionsList;
    private int counter;
    private int loopCounter;

    public HardWiredAIService() {
        desiredActionsList = new ArrayList<DesiredActions>();
        counter = 0;
        loopCounter = 0;
        desiredActionsList.add(buildDesiredActions(0, 0.0));
        desiredActionsList.add(buildDesiredActions(0, 0.25));
        desiredActionsList.add(buildDesiredActions(0.0, 0.3));
        desiredActionsList.add(buildDesiredActions(0.0, 0.25));
        desiredActionsList.add(buildDesiredActions(0.0, 0.0));
        desiredActionsList.add(buildDesiredActions(0.0, 0.0));
    }

    @Override
    public DesiredActions getDesiredActions() {
        if (counter >= desiredActionsList.size()) {
            LOGGER.info("STOPPED !!!!");
            return buildDesiredActions(0, 0);
        } else {
            if (loopCounter++ >= MAX_LOOP) {
                loopCounter = 0;
                counter++;
            }
            LOGGER.info(String.format("Counter: %s - Loopcounter: %s", counter, loopCounter));
            return desiredActionsList.get(counter);
        }
    }

    private DesiredActions buildDesiredActions(final double speed, final double direction) {
        return new DesiredActions(new SteeringAction(direction), new ThrottleAction(speed));
    }
}
