package nl.jpoint.trojkaracer.car.domain.computervision;

import org.opencv.core.Scalar;

/**
 * Enumeration of the parameters that are used during the computer vision calculations.
 */
public enum ComputerVisionParameters {

    CANNY_THRESHOLD1(Integer.class),
    CANNY_THRESHOLD2(Integer.class),
    CANNY_APERTURE_SIZE(Integer.class),
    CANNY_GRADIENT(Boolean.class),

    BLUR_SIZE(Integer.class),

    HOUGH_RHO(Double.class),
    HOUGH_THETA(Double.class),
    HOUGH_THRESHOLD(Integer.class),
    HOUGH_MIN_LINE_LENGTH(Integer.class),
    HOUGH_MAX_GAP_SIZE(Integer.class),

    DETECT_LINE_SLOPE_MIN(Double.class),
    DETECT_LINE_SLOPE_MAX(Double.class),

    DRAW_LINE_COLOR(Scalar.class),
    DRAW_LINE_THICKNESS(Integer.class),

    LANE_LINE_GRAY_LOWER(Scalar.class),
    LANE_LINE_GRAY_UPPER(Scalar.class),
    LANE_LINE_COLOR_LOWER(Scalar.class),
    LANE_LINE_COLOR_UPPER(Scalar.class),

    ROI_X_OFFSET_PERCENTAGE(Double.class),
    ROI_Y_TOP_PERCENTAGE(Double.class),
    ROI_Y_MIDDLE_PERCENTAGE(Double.class),
    ROI_Y_BOTTOM_PERCENTAGE(Double.class);

    private Class clazz;

    ComputerVisionParameters(final Class clazz) {
         this.clazz = clazz;
    }

    public Class getClazz() {
        return clazz;
    }
}
