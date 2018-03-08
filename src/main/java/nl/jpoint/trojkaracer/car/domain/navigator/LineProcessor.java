package nl.jpoint.trojkaracer.car.domain.navigator;

import nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionHelper;
import nl.jpoint.trojkaracer.car.domain.computervision.Line;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class LineProcessor {

    private final double MIN_X = 0d;
    private final double MAX_X = 640d;
    private final double MID_X = 320d;

    private final double MAX_Y = 480d;

    private final double SCAN_Y_MAX = 360d;
    private final int SCAN_STEP_SIZE = 4;

    private final int LINE_FUZZ = 6;

    /*
    Based on input lines, output the desired direction.
     */
    public NavigationDirections process(final List<Line> lines, ComputerVisionHelper computerVisionHelper, Mat frame) {

        int scan_min_y = lines
                .stream()
                .map(line -> Math.min(line.getY1(), line.getY2()))
                .mapToInt(a->a.intValue())
                .min()
                .getAsInt();

        //Assume we are on the track, in the 'middle'
        double avgMiddle = MID_X;

        List<Double> middles = new ArrayList<>();
        middles.add(avgMiddle);

        boolean hasFoundBothSidesOnce = false;

        // Scan from 'bottom' of interest up:
        for(double ty = SCAN_Y_MAX; ty > scan_min_y; ty -= SCAN_STEP_SIZE) {

            // Scan lines:
            // Per horizontal line, pick left and right points.
            Double closestLeft = null;
            Double closestRight = null;

            Line horizontal = new Line(MIN_X, ty, MAX_X, ty);
            for(Line line:lines) {

                boolean fits1 = ty+LINE_FUZZ > line.getY1() && ty-LINE_FUZZ < line.getY2();
                boolean fits2 = ty+LINE_FUZZ > line.getY2() && ty-LINE_FUZZ < line.getY1();

                if(!fits1 && !fits2) {
                    continue;
                }

                Point2D intersection = line.getLineLineIntersection(horizontal);
                if(intersection != null) {
                    //Determine left or right of middle:
                    if(intersection.getX() < avgMiddle) {
                        if(closestLeft == null) {
                            closestLeft = intersection.getX();
                        } else {
                            closestLeft = Math.max(closestLeft, intersection.getX());
                        }
                    } else {
                        if(closestRight == null) {
                            closestRight = intersection.getX();
                        } else {
                            closestRight = Math.min(closestRight, intersection.getX());
                        }
                    }
                }
            }

            if(closestLeft == null && closestRight == null) {
                continue;
            }

            if((closestLeft != null && closestRight != null)) {
                hasFoundBothSidesOnce = true;
            }

            if(closestLeft != null && closestRight != null || !hasFoundBothSidesOnce) {
                if(closestLeft == null) {
                    closestLeft = MIN_X;
                }
                if(closestRight == null) {
                    closestRight = MAX_X;
                }

                double middle = (closestLeft + closestRight) / 2;

                if(frame != null) {
                    // Draw for debug:
                    Line lineLeft = new Line(avgMiddle, ty, closestLeft, ty);
                    lineLeft.draw(frame, new Scalar(255, 255, 0), 1);
                    Line lineRight = new Line(avgMiddle, ty, closestRight, ty);
                    lineRight.draw(frame, new Scalar(255, 0, 0), 1);
                }

                // Update for next round:
                middles.add(middle);
                if(middles.size() < 5) {
                    avgMiddle = middles.stream().mapToDouble(a -> a).average().getAsDouble();
                } else {
                    avgMiddle = middles.subList(middles.size() / 2, middles.size()).stream().mapToDouble(a -> a).average().getAsDouble();
                }

            }
        }

        // Calculate the final average:
        double avgTop = middles.subList(middles.size() / 2, middles.size()).stream().mapToDouble(a -> a).average().getAsDouble();
        double carMiddle = MID_X;

        // Calculate the final drive-line:
        Line driveLine = new Line(carMiddle, MAX_Y, avgTop, scan_min_y);

        if(frame != null) {
            lines.forEach(line -> line.draw(frame, new Scalar(0, 0, 255), 3));
            driveLine.draw(frame, new Scalar(0, 255, 0), 5);
            computerVisionHelper.writeImage(frame, "test-lanedetection");
        }

        //Calculate angle:
        double xDiff = driveLine.getX1() - driveLine.getX2();
        double yDiff = driveLine.getY1() - driveLine.getY2();
        double driveAngle = Math.toDegrees(Math.atan2(yDiff, xDiff)) - 90;

        // Normally a value between: -60 and 60
        return NavigationDirections.of((int) driveAngle);
    }
}
