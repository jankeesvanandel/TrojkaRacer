package nl.jpoint.trojkaracer.processing;

import javax.inject.Inject;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ImageProcessor implements Runnable {

    // TODO: These properties should come from properties-project:
    // Square area we're detecting as containing traffic light:
    private static final int LIGHT_SIZE = 20;
    // Color of the track boundaries (in BGR):
    private static final int[] TRACK_TAPE_COLOR = new int[] {230 ,230, 230};
    // Color that kind of matches the red light (in BGR):
    private static final int[] RED_LIGHT_COLOR = new int[] {10 ,10, 155};
    // Color that matches the green light (in BGR):
    private static final int[] GREEN_LIGHT_COLOR = new int[] {67 ,143, 74};

    // Looping in pixel arrays the step is 3 (B/G/R)
    private static final int PIXEL_STEP = 3;

    private ImageReader imageReader;

    @Inject
    public ImageProcessor(ImageReader imageReader) {
        this.imageReader = imageReader;
    }

    // Mutated data:
    private TrackBoundaries latestComputedBoundaries = null;
    private Point2D trafficLightLocation = null;
    private boolean waitingForGreenLight = true;

    // Getters:
    public TrackBoundaries getLatestTrackBoundaries() {
        return latestComputedBoundaries;
    }

    public Point2D getTrafficLightLocation() {
        return trafficLightLocation;
    }

    public boolean isWaitingForGreenLight() {
        return waitingForGreenLight;
    }

    /**
     * When this processor is launched it will:
     *
     * - Find the reddest area on the webcam and assume a red light (don't drive)
     * - Monitor this spot until it becomes green
     * - Calculate the track boundaries each loop
     */
    public void run() {

        // 1) Fetch image from webcam
        BufferedImage image = imageReader.fetchImage();

        // 2) Analyze image
        final int pixelLength = 3;
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        if(trafficLightLocation == null) {
            // We're starting the processor, no traffic light has been found yet:
            trafficLightLocation = findRedTrafficLightLocation(image, pixels);
        } else if(waitingForGreenLight) {
            // We're waiting for the light to turn green, check the color:
            if(trafficLightTurnedGreen(image, pixels)) {
                waitingForGreenLight = false;
            }
        }

        // Calculate the track boundaries

    }




    /**
     *
     * This method loops over the pixels and finds an area of LIGHT_SIZE that is closed to RED_LIGHT_COLOR.
     *
     * @param image
     * @param pixels
     */
    private Point2D findRedTrafficLightLocation(BufferedImage image, byte[] pixels) {

        double lowestError = Double.MAX_VALUE;
        Point2D trafficLightLocation = null;

        int step = LIGHT_SIZE / 4;
        // IMPROVEMENT? Don't scan every pixel, to improve performance, maybe use 2, 3 as step?
        // IMPROVEMENT? Maybe don't scan the entire image for the traffic light? Just the top half?

        for(int y = 0; y < (image.getHeight() - LIGHT_SIZE); y += step) {
            for(int x = 0; x < (image.getWidth() - LIGHT_SIZE); x += step) {

                double locationError = 0;

                for(int yOff = 0; yOff < LIGHT_SIZE; yOff++) {
                    for(int xOff = 0; xOff < LIGHT_SIZE; xOff++) {
                        int pixelLocation = calculatePixelLocation(image, y, x, yOff, xOff);

                        locationError += colorDifference(getBGR(pixels, pixelLocation), RED_LIGHT_COLOR);
                    }
                }

                if(locationError < lowestError) {
                    lowestError = locationError;
                    trafficLightLocation = new Point2D.Double(x + (LIGHT_SIZE/2), y + (LIGHT_SIZE/2));
                }
            }
        }
        return trafficLightLocation;
    }

    private int calculatePixelLocation(BufferedImage image, int y, int x, int yOff, int xOff) {
        return ((y + yOff) * image.getWidth() * PIXEL_STEP) + ((x + xOff) * PIXEL_STEP);
    }

    /**
     * This method compares the area found by the red light detector to see if it is more green than red.
     *
     * @param image
     * @param pixels
     * @return
     */
    private boolean trafficLightTurnedGreen(BufferedImage image, byte[] pixels) {

        int x = (int) trafficLightLocation.getX();
        int y = (int) trafficLightLocation.getY();

        double redError = 0;
        double greenError = 0;

        for(int yOff = -(LIGHT_SIZE/2); yOff < (LIGHT_SIZE/2); yOff++) {
            for(int xOff = -(LIGHT_SIZE/2); xOff < (LIGHT_SIZE/2); xOff++) {

                int pixelLocation = calculatePixelLocation(image, y, x, yOff, xOff);
                int[] pixel = getBGR(pixels, pixelLocation);

                redError += colorDifference(pixel, RED_LIGHT_COLOR);
                greenError += colorDifference(pixel, GREEN_LIGHT_COLOR);
            }
        }

        // If the area of the traffic light is closer to green than it is to red, GO!
        return (greenError < redError);
    }

    /**
     * Extract the color information for a given location (in BGR, blue/green/red format).
     *
     * @param pixels
     * @param pixelLocation
     * @return
     */
    private int[] getBGR(byte[] pixels, int pixelLocation) {
        return new int[] {
                pixels[pixelLocation] & 0xFF,
                pixels[pixelLocation + 1] & 0xFF,
                pixels[pixelLocation + 2] & 0xFF
        };
    }

    /**
     * Calculate the mean squared error between two pixel colors.
     *
     * @param p1 First pixel
     * @param p2 Second pixel
     * @return Mean squared error
     */
    private double colorDifference(int[] p1, int[] p2) {
        return Math.sqrt(
                (p2[0]-p1[0])*(p2[0]-p1[0]) +
                (p2[1]-p1[1])*(p2[1]-p1[1]) +
                (p2[2]-p1[2])*(p2[2]-p1[2])
                );
    }

}
