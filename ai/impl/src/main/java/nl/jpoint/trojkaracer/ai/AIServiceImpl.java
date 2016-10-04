package nl.jpoint.trojkaracer.ai;

import nl.jpoint.trojkaracer.processing.ProcessingService;
import nl.jpoint.trojkaracer.processing.TrackBoundaries;
import nl.jpoint.trojkaracer.processing.TrackInfo;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.IntStream;

public class AIServiceImpl implements AIService {

    private static final double THROTTLE_FACTOR = 40.0d;
    private static final double MAX_THROTTLE = 1.0d;
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
            final double[] steeringAndThrottle = calculateSteeringAndThrottle(trackInfo);
            desiredActions =
                    new DesiredActions(
                        new SteeringAction(steeringAndThrottle[0]),
                        new ThrottleAction(steeringAndThrottle[1]));
        }
        return desiredActions;
    }

    private double[] calculateSteeringAndThrottle(TrackInfo trackInfo) {
        TrackBoundaries boundaries = trackInfo.getBoundaries();
        List<int[]> scannedLines = boundaries.getScannedLines();
        double nextX = 0, nextY = 0;
        double currentX = 0, currentY = 0;
        boolean firstVisibleTrackPart = true;
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
