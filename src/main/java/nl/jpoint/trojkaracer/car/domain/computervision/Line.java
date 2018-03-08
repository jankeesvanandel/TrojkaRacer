package nl.jpoint.trojkaracer.car.domain.computervision;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Object representing a line and offering methods to draw a line on an image.
 */
public class Line {

    static final double MAX_SLOPE = 1000000d;

    private final double x1;
    private final double y1;
    private final double x2;
    private final double y2;

    private final double slope;

    /**
     * Constructor the initializes the line with the two coordinates.
     * @param x1 the x-coordinate of the first point of the line.
     * @param y1 the y-coordinate of the first point of the line.
     * @param x2 the x-coordinate of the second point of the line.
     * @param y2 the y-coordinate of the second point of the line.
     */
    Line(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;

        slope = Math.min(Math.abs((y2 - y1) / Math.max(0.0000001, x2 - x1)), MAX_SLOPE);
    }

    /**
     * Returns the slope of a line.
     * @return the slope of a line.
     */
    public double getSlope() {
        return slope;
    }

    public double getX1() {
        return x1;
    }

    public double getX2() {
        return x2;
    }

    public double getY1() {
        return y1;
    }

    public double getY2() {
        return y2;
    }

    /**
     * Draws the line on the given image.
     * @param image the image on which the line is to be drawn.
     * @param color the color the line should be drawn in.
     * @param thickness the thickness of the line to be drawn.
     */
    void draw(final Mat image, final Scalar color, final int thickness) {
        Imgproc.line(image, new Point(x1, y1), new Point(x2, y2), color, thickness);
    }
}
