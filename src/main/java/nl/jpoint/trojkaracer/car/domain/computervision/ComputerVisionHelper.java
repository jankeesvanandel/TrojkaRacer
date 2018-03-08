package nl.jpoint.trojkaracer.car.domain.computervision;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.BLUR_SIZE;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.CANNY_APERTURE_SIZE;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.CANNY_GRADIENT;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.CANNY_THRESHOLD1;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.CANNY_THRESHOLD2;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.DETECT_LINE_SLOPE_MAX;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.DETECT_LINE_SLOPE_MIN;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.DRAW_LINE_COLOR;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.DRAW_LINE_THICKNESS;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.HOUGH_MAX_GAP_SIZE;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.HOUGH_MIN_LINE_LENGTH;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.HOUGH_RHO;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.HOUGH_THETA;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.HOUGH_THRESHOLD;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.LANE_LINE_COLOR_LOWER;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.LANE_LINE_COLOR_UPPER;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.LANE_LINE_GRAY_LOWER;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.LANE_LINE_GRAY_UPPER;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.ROI_X_OFFSET_PERCENTAGE;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.ROI_Y_BOTTOM_PERCENTAGE;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.ROI_Y_MIDDLE_PERCENTAGE;
import static nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionParameters.ROI_Y_TOP_PERCENTAGE;

/**
 * Helper class that offers several computer vision related operations for converting images (as in {@link Mat} instances), applying masks
 * or retrieving other information.
 */
public class ComputerVisionHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String imageStoragePath;
    private final Map<ComputerVisionParameters, Object> parameters =
            Collections.synchronizedMap((Map<ComputerVisionParameters, Object>) new EnumMap(ComputerVisionParameters.class));

    /**
     * Creates a new instance with all the default parameters set.
     * @param imageStoragePath the path to which images are written.
     */
    public ComputerVisionHelper(final String imageStoragePath) {
        this.imageStoragePath = imageStoragePath;

        parameters.put(CANNY_THRESHOLD1, 50);
        parameters.put(CANNY_THRESHOLD2, 180);
        parameters.put(CANNY_APERTURE_SIZE, 3);
        parameters.put(CANNY_GRADIENT, false);

        parameters.put(BLUR_SIZE, 5);

        parameters.put(HOUGH_RHO, 2d);
        parameters.put(HOUGH_THETA, Math.PI / 180);
        parameters.put(HOUGH_THRESHOLD, 1);
        parameters.put(HOUGH_MIN_LINE_LENGTH, 8);
        parameters.put(HOUGH_MAX_GAP_SIZE, 5);

        parameters.put(DETECT_LINE_SLOPE_MIN, 0.2);
        parameters.put(DETECT_LINE_SLOPE_MAX, 15d);

        parameters.put(DRAW_LINE_COLOR, new Scalar(0, 0, 255));
        parameters.put(DRAW_LINE_THICKNESS, 3);

        parameters.put(LANE_LINE_GRAY_LOWER, new Scalar(200));
        parameters.put(LANE_LINE_GRAY_UPPER, new Scalar(255));
        parameters.put(LANE_LINE_COLOR_LOWER, new Scalar(100, 5, 75));
        parameters.put(LANE_LINE_COLOR_UPPER, new Scalar(180, 30, 250));

        parameters.put(ROI_X_OFFSET_PERCENTAGE, 0.0d);
        parameters.put(ROI_Y_TOP_PERCENTAGE, 0.51d);
        parameters.put(ROI_Y_MIDDLE_PERCENTAGE, 0.85d);
        parameters.put(ROI_Y_BOTTOM_PERCENTAGE, 1d);
    }

    /**
     * Detects the lanes in the image and returns the same image with the lanes drawn in it.
     * @param image the image to detect and add the lanes to.
     * @return the image with the detected lanes drawn on it.
     */
    public Mat addLanesToImage(final Mat image) {
        final List<Line> laneLines = getLaneLines(image);

        filterLines(laneLines)
                .forEach(line -> line.draw(image, (Scalar) parameters.get(DRAW_LINE_COLOR), (Integer) parameters.get(DRAW_LINE_THICKNESS)));
        return image;
    }

    /**
     * This function take as input a color image and tries to infer the lane lines in the image.
     *
     * @param image input frame/image.
     * @return list of lane lines.
     */
    public List<Line> getLaneLines(final Mat image) {
        final Mat roiMask = getRegionOfInterestMask(image, Collections.singletonList(new MatOfPoint(getROIPoints(image.size()))));
        final Mat imageOfInterest = applyMask(image, roiMask);

        final Mat grayImage = convertBGRToGrayScale(imageOfInterest);
        final Mat blurImage = removeNoise(grayImage);
        final Mat edgeImage = cannyEdgeDetection(blurImage);

        final Mat detectedLinesMatrix = houghLines(edgeImage);

        final List<Line> detectedLines = new ArrayList<>(detectedLinesMatrix.rows());
        for (int i = 0; i < detectedLinesMatrix.rows(); i++) {
            double[] val = detectedLinesMatrix.get(i, 0);
            detectedLines.add(new Line(val[0], val[1], val[2], val[3]));
        }

        return detectedLines;
    }

    public List<Line> filterLines(final List<Line> lines) {
        return filterLines(lines,
                (double) parameters.get(DETECT_LINE_SLOPE_MIN),
                (double) parameters.get(DETECT_LINE_SLOPE_MAX));
    }

    public List<Line> filterLines(final List<Line> lines, double lowSlope, double highSlope) {
        return lines.stream()
                .filter(line -> line.getSlope() >= lowSlope && line.getSlope() <= highSlope)
                .collect(Collectors.toList());
    }

    /**
     * Returns a Mask image that selects the region of interest as marked by the provided vertices.
     * @param image an image that defines the size, type and amount of channels to use in the mask.
     * @param vertices a list of vertices that defines the contours of the mask.
     * @return the mask image.
     */
    public Mat getRegionOfInterestMask(final Mat image, final List<MatOfPoint> vertices) {
        final Mat mask = new Mat(image.size(), image.type());
        final Scalar ignoreMaskColor;

        switch (image.channels()) {
            case 1:
                ignoreMaskColor = new Scalar(255);
                break;
            case 2:
                ignoreMaskColor = new Scalar(255, 255);
                break;
            case 3:
                ignoreMaskColor = new Scalar(255, 255, 255);
                break;
            default:
                ignoreMaskColor = new Scalar(255, 255, 255, 255);
                break;
        }

        final Mat result = new Mat();
        Imgproc.fillPoly(mask, vertices, ignoreMaskColor);
        Core.bitwise_and(image, mask, result);
        return result;
    }

    /**
     * Applies the given mask on the image and returns the (masked) result.
     * @param image the image to apply the mask on.
     * @param mask the mask to apply to the image.
     * @return the masked image.
     */
    public Mat applyMask(final Mat image, final Mat mask) {
        final Mat result = new Mat();
        Core.bitwise_and(image, mask, result);

        return result;
    }

    /**
     * Converts the provided BGR image into a grayscale image.
     * @param image the image to convert.
     * @return the GrayScale image.
     */
    public Mat convertBGRToGrayScale(final Mat image) {
        final Mat result = new Mat();
        Imgproc.cvtColor(image, result, Imgproc.COLOR_BGR2GRAY);

        return result;
    }

    /**
     * Converts the provided grayscale image into a color (BGR) image.
     * @param image the image to convert.
     * @return the BGR color image.
     */
    public Mat convertGrayScaleToBGRColor(final Mat image) {
        final Mat result = new Mat();
        Imgproc.cvtColor(image, result, Imgproc.COLOR_GRAY2BGR);

        return result;
    }

    /**
     * Converts the provided BGR image into a HSV image.
     * @param image the image to convert.
     * @return the HSV image.
     */
    public Mat convertBGRColorToHSV(final Mat image) {
        final Mat result = new Mat();
        Imgproc.cvtColor(image, result, Imgproc.COLOR_BGR2HSV);

        return result;
    }

    /**
     * Returns an image with some of the noise removed (due to blurring of the image).
     * @param image the image to remove the noise in.
     * @return the image with the noise removed.
     */
    public Mat removeNoise(final Mat image) {
        return removeNoise(image,
                (Integer) parameters.get(BLUR_SIZE),
                (Integer) parameters.get(BLUR_SIZE));
    }

    /**
     * Returns an image with some of the noise removed (due to blurring of the image).
     * @param image the image to remove the noise in.
     * @return the image with the noise removed.
     */
    public Mat removeNoise(final Mat image, int blurX, int blurY) {
        final Mat result = new Mat();
        org.opencv.imgproc.Imgproc.GaussianBlur(image, result, new Size(blurX, blurY), 0, 0);

        return result;
    }

    /**
     * Returns an image showing the edges of the given image.
     * @param image the source image of which the edges are to be detected,
     * @return an image with the edges shown.
     */
    public Mat cannyEdgeDetection(final Mat image) {
        return cannyEdgeDetection(image,
                (Integer) parameters.get(CANNY_THRESHOLD1),
                (Integer) parameters.get(CANNY_THRESHOLD1),
                (Integer) parameters.get(CANNY_APERTURE_SIZE),
                (Boolean) parameters.get(CANNY_GRADIENT));
    }

    /**
     * Returns an image showing the edges of the given image.
     * @param image the source image of which the edges are to be detected,
     * @return an image with the edges shown.
     */
    public Mat cannyEdgeDetection(final Mat image, int threshold1, int threshold2, int aperture, boolean gradient) {
        final Mat result = new Mat();
        Imgproc.Canny(image, result, threshold1, threshold2, aperture, gradient);

        return result;
    }

    /**
     * Returns a Matrix with the detected lines in it.
     * @param cannyImage (grayscale) image that is the result of a Canny edge detection.
     * @return a one dimensial Matrix (only 1 single row) containing all the lines as 4 size double arrays (describing the two points of the line).
     */
    public Mat houghLines(final Mat cannyImage) {
        return houghLines(cannyImage,
                (Double) parameters.get(HOUGH_RHO),
                (Double) parameters.get(HOUGH_THETA),
                (Integer) parameters.get(HOUGH_THRESHOLD),
                (Integer) parameters.get(HOUGH_MIN_LINE_LENGTH),
                (Integer) parameters.get(HOUGH_MAX_GAP_SIZE));
    }

    /**
     * Returns a Matrix with the detected lines in it.
     * @param cannyImage (grayscale) image that is the result of a Canny edge detection.
     * @return a one dimensial Matrix (only 1 single row) containing all the lines as 4 size double arrays (describing the two points of the line).
     */
    public Mat houghLines(final Mat cannyImage, double rho, double theta, int threshold, int minLineLength, int maxGapSize) {
        final Mat lines = new Mat();
        Imgproc.HoughLinesP(cannyImage, lines, rho, theta, threshold, minLineLength, maxGapSize);

        return lines;
    }

    /**
     * Writes the image to disk.
     * @param image the image to write to disk.
     * @param fileName the base filename to use.
     */
    public void writeImage(final Mat image, final String fileName) {
        Imgcodecs.imwrite(String.format("%1$s%2$s-%3$tH%3$tM%3$tS.%3$tL.jpg", imageStoragePath, fileName, new Date()), image);
    }

    /**
     * Returns the Region of Interest as an array of {@link Point}s.
     * @param size the size of the image to which the ROI applies.
     * @return an array of Points that together make up the ROI.
     */
    private Point[] getROIPoints(final Size size) {
        final int xOffset = (int) (size.width * (Double) parameters.get(ROI_X_OFFSET_PERCENTAGE));

        final int xLeft = xOffset;
        final int xRight = (int) size.width - xOffset;
        final int yTop = (int) (size.height * (Double) parameters.get(ROI_Y_TOP_PERCENTAGE));
        final int yMiddle = (int) (size.height * (Double) parameters.get(ROI_Y_MIDDLE_PERCENTAGE));
        final int yBottom = (int) (size.height * (Double) parameters.get(ROI_Y_BOTTOM_PERCENTAGE));

        return new Point[] {
                new Point(0, yBottom),
                new Point(0, yMiddle),
                new Point(xLeft, yTop),
                new Point(xRight, yTop),
                new Point(size.width, yMiddle),
                new Point(size.width, yBottom)
        };
    }

    /**
     * Returns the Hue value for a RGB value.
     * @return the Hue value for a RGB value.
     */
    public int getHueForRGB(final int r, final int g, final int b) {
        final Scalar rgb = new Scalar(b, g, r);
        final Mat image = new Mat(1, 1, 16);
        image.setTo(rgb);

        final Mat hsvImage = convertBGRColorToHSV(image);
        return (int) hsvImage.get(0, 0)[0];
    }

    /**
     * Returns an image with only the parts of the image shown that have a hue close to the given hue.
     *
     * @param image the source image.
     * @param hue the hue to select.
     * @return an image with only the parts of the image shown that have a hue close to the given hue.
     */
    public Mat filterToColor(final Mat image, final int hue) {
        return filterToColor(image, new Scalar(hue - 10, 100, 100), new Scalar(hue + 10, 255, 255));
    }

    /**
     * Returns an image with only the parts of the image shown that (in HSV mode) have a color between the two provided colors.
     *
     * @param image the source image.
     * @param lowerColorBoundary the lower color boundary.
     * @param upperColorBoundary the upper color boundary.
     * @return an image with only the parts of the image shown that have a hue close to the given hue.
     */
    public Mat filterToColor(final Mat image, final Scalar lowerColorBoundary, final Scalar upperColorBoundary) {
        final Mat hsvImage = new Mat();
        final Mat colorMask = new Mat();

        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);
        Core.inRange(hsvImage, lowerColorBoundary, upperColorBoundary, colorMask);
        return applyMask(convertBGRToGrayScale(image), colorMask);
    }

    /**
     * Returns the amount of black pixels in the given image.
     * @param image the image to check the pixels
     * @return the amount of black pixels.
     */
    public int findAmountOfNonBlackPixels(final Mat image) {
        return Core.countNonZero(image);
    }

    public Object getParameter(Object key) {
        return parameters.get(key);
    }
}
