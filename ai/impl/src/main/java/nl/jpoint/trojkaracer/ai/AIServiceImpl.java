package nl.jpoint.trojkaracer.ai;

import nl.jpoint.trojkaracer.processing.ProcessingService;
import nl.jpoint.trojkaracer.processing.TrackBoundaries;
import nl.jpoint.trojkaracer.processing.TrackInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.List;
import java.util.stream.IntStream;

@Singleton
public class AIServiceImpl implements AIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AIServiceImpl.class);

    private static final double THROTTLE_FACTOR = 40.0d;
    private static final double MAX_THROTTLE = 0.2d;
    private ProcessingService processingService;

    private long lastProcessedTimestamp = -1;
    private DesiredActions desiredActions;

    @Inject
    public AIServiceImpl(ProcessingService processingService) {
        this.processingService = processingService;
    }

    @Override
    public DesiredActions getDesiredActions() {
        LOGGER.debug("Retrieving new desired actions");
        TrackInfo trackInfo = processingService.getTrackInfo();

        if(trackInfo.getTimestamp() > lastProcessedTimestamp) {
            // Process new trackinfo:

            // Maybe we should make the DesiredActions hold just the double values instead of these wrapper items?
            final double[] steeringAndThrottle = calculateSteeringAndThrottle(trackInfo);
            desiredActions =
                    new DesiredActions(
                        new SteeringAction(steeringAndThrottle[0]),
                        new ThrottleAction(steeringAndThrottle[1]));
        } else {
            LOGGER.debug("Not creating new desired actions as track info wasn't new compared to last processed timestamp.");
        }
        LOGGER.debug("Returning new desired actions of {} and {}", desiredActions.getThrottleAction().getThrottleAmount(),
                desiredActions.getSteeringAction().getSteeringPosition());
        return desiredActions;
    }

    private double[] calculateSteeringAndThrottle(TrackInfo trackInfo) {
        TrackBoundaries boundaries = trackInfo.getBoundaries();
        if (boundaries == null) {
            LOGGER.debug("No boundaries detected for trackinfo {}; setting desired action to neutral/stopped position", trackInfo.getTimestamp());
            return new double[] { 0.0, 0.0 };
        }

        List<int[]> scannedLines = boundaries.getScannedLines();
        LOGGER.debug("C");
        double nextX = 0, nextY = 0;
        double currentX = 0, currentY = 0;
        boolean firstVisibleTrackPart = true;
        int i = 0;
        for (int[] scannedLine : scannedLines) {
            if (scannedLine.length == 0) {
                // No more data, abort.
                break;
            }

            float middle = calculateMiddle(scannedLine);
            if (firstVisibleTrackPart) {
                firstVisibleTrackPart = false;
                currentX = middle;
                currentY = nextY;
            }

            nextY++;
            nextX = nextX + (middle - nextX) / nextY;
        }

        double angle = getAngle(currentX, currentY, nextX, nextY);
        double throttle = Math.min((nextY - currentY) / THROTTLE_FACTOR, MAX_THROTTLE);
        return new double[] { angle, throttle };
    }

    private float calculateMiddle(final int[] scannedLine) {
        float middle;
        if (scannedLine.length == 1) {
            middle = scannedLine[0];
        } else if (scannedLine.length == 2) {
            middle = scannedLine[0] + scannedLine[1] / 2;
        } else if (scannedLine.length == 3) {
            middle = scannedLine[0] + scannedLine[1] + scannedLine[2] / 2;
        } else {
            middle = (float) IntStream.of(scannedLine).average().getAsDouble();
        }
        return middle;
    }

    /*
     * Return the angle of the two given points, as a double between -1.0 and 1.0.
     */
    private double getAngle(double x1, double y1, double x2, double y2) {
        return (Math.atan2(x2 - x1, y2 - y1) / Math.PI) * 2;
    }

}
