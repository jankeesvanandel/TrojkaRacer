package nl.jpoint.trojkaracer.car.domain.computervision;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.awt.geom.Point2D;

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
    public Line(double x1, double y1, double x2, double y2) {
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

    public Point2D getLineLineIntersection(Line line) {
        double det1And2 = det(x1, y1, x2, y2);
        double det3And4 = det(line.getX1(), line.getY1(), line.getX2(), line.getY2());
        double x1LessX2 = x1 - x2;
        double y1LessY2 = y1 - y2;
        double x3LessX4 = line.getX1() - line.getX2();
        double y3LessY4 = line.getY1() - line.getY2();
        double det1Less2And3Less4 = det(x1LessX2, y1LessY2, x3LessX4, y3LessY4);
        if (det1Less2And3Less4 == 0){
            // the denominator is zero so the lines are parallel and there's either no solution (or multiple solutions if the lines overlap) so return null.
            return null;
        }
        double x = (det(det1And2, x1LessX2,
                det3And4, x3LessX4) /
                det1Less2And3Less4);
        double y = (det(det1And2, y1LessY2,
                det3And4, y3LessY4) /
                det1Less2And3Less4);
        return new Point2D.Double(x, y);
    }
    protected static double det(double a, double b, double c, double d) {
        return a * d - b * c;
    }

    /**
     * Draws the line on the given image.
     * @param image the image on which the line is to be drawn.
     * @param color the color the line should be drawn in.
     * @param thickness the thickness of the line to be drawn.
     */
    public void draw(final Mat image, final Scalar color, final int thickness) {
        Imgproc.line(image, new Point(x1, y1), new Point(x2, y2), color, thickness);
    }
}
