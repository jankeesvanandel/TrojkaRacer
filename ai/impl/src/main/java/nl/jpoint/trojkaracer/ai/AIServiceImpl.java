package nl.jpoint.trojkaracer.ai;

import nl.jpoint.trojkaracer.processing.ProcessingService;
import nl.jpoint.trojkaracer.processing.TrackInfo;

import javax.inject.Inject;

public class AIServiceImpl implements AIService {

    private ProcessingService processingService;

    private long lastProcessedTimestamp = -1;
    private DesiredActions desiredActions;

    @Inject
    public AIServiceImpl(ProcessingService processingService) {
        this.processingService = processingService;
    }

    @Override
    public DesiredActions getDesiredActions() {

        TrackInfo trackInfo = processingService.getTrackInfo();
        if(trackInfo.getTimestamp() > lastProcessedTimestamp) {
            // Process new trackinfo:

            // Maybe we should make the DesiredActions hold just the double values instead of these wrapper items?
            desiredActions =
                    new DesiredActions(
                        new SteeringAction(calculateSteeringAngle(trackInfo)),
                        new ThrottleAction(calculateThrottle(trackInfo)));
        }
        return desiredActions;
    }

    private double calculateSteeringAngle(TrackInfo trackInfo) {
        // TODO:
        // Analyse the track/tape boundaries and return an average position/target
        // Because the camera is mounted on the car *our* position is always in the middle of the image.
        // So the line we should take is from the center of the image to the center of the track (as far away as possible?)
        return 0.0;
    }

    private double calculateThrottle(TrackInfo trackInfo) {
        if(trackInfo.isStartSignRed()) {
            // Red light, no driving! (We should steer though, that makes it easier while testing)
            return 0.0;
        } else {
            // TODO:
            // We should detect horizontal lines/finish area's?
            // This is pretty hard to do though...
            // Combined with the track boundaries we might be able to detect the finish area and calculate the distance to it.
            return 0.0;
        }

    }
}
