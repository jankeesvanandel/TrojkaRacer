package nl.jpoint.trojkaracer.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageProcessor implements Runnable {

    // ----------------------------------------------------------------
    // TODO: These properties should come from properties-project, injected here?
    // Square area we're detecting as containing traffic light:
    private static final int LIGHT_SIZE = 20;

    // Amount of consecutive pixels to make up a border-tape:
    private static final int TAPE_WIDTH_THRESHOLD = 4;
    // Mean sqrt color error between TAPE_COLOR and camera
    private static final double TAPE_PIXEL_ERROR_THRESHOLD = 80;

    // Color of the boundaries tape (in BGR):
    private static final int[] TAPE_COLOR = new int[] {20 ,20, 20};
    // Color that kind of matches the red light (in BGR):
    private static final int[] RED_LIGHT_COLOR = new int[] {10 ,10, 155};
    // Color that matches the green light (in BGR):
    private static final int[] GREEN_LIGHT_COLOR = new int[] {67 ,143, 74};
    // Horizon of the (flat) road, don't look above this line:
    private static final int WEBCAM_HORIZON = 120;
    // ----------------------------------------------------------------

    // Looping in pixel arrays the step is 3 (B/G/R)
    private static final int PIXEL_STEP = 3;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageProcessor.class);

    private ImageReader imageReader;

    @Inject
    public ImageProcessor(ImageReader imageReader) {
        this.imageReader = imageReader;
    }

    // Mutated data:
    private TrackBoundaries trackBoundaries = null;
    private Point2D trafficLightLocation = null;
    private boolean waitingForGreenLight = true;

    // Getters:
    public TrackBoundaries getLatestTrackBoundaries() {
        return trackBoundaries;
    }

    public Point2D getTrafficLightLocation() {
        return trafficLightLocation;
    }

    public boolean isWaitingForGreenLight() {
        return waitingForGreenLight;
    }

    public ImageProcessor withNoEyeForTrafficLights() {
        trafficLightLocation = new Point2D.Double(0.0, 0.0);
        waitingForGreenLight = false;
        return this;
    }

    /**
     * When this processor is launched it will:
     *
     * - Find the reddest area on the webcam and assume a red light (don't drive)
     * - Monitor this spot until it becomes green
     * - Calculate the track boundaries each loop
     *
     * TODO: Create a way so this method is called and loops all the time
     * TODO: How are we bootstrapping all the code?
     */
    public void run() {
        LOGGER.debug("Fetching new image");
        // Fetch image from webcam:
        BufferedImage image = imageReader.fetchImage();

        if (image == null) {
            LOGGER.warn("Failed to fetch image from the imageReader; not analyzing any image.");
            return;
        }


        // Analyze image:

        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        if(trafficLightLocation == null) {
            LOGGER.info("Searching for traffic light..");
            // We're starting, no traffic light has been found yet:
            trafficLightLocation = findRedTrafficLightLocation(image, pixels);
            LOGGER.debug("Traffic light location set to {}", trafficLightLocation);
            // Calculate the track once (when we start we have a track):
            trackBoundaries = calculateTrackBoundaries(image, pixels);
        } else if(waitingForGreenLight) {
            // We're waiting for the light to turn green, check the color:
            if(trafficLightTurnedGreen(image, pixels)) {
                LOGGER.info("Red lights are off....Let's GO !!!");
                waitingForGreenLight = false;
            }
        } else {
            // Calculate the track boundaries:
            trackBoundaries = calculateTrackBoundaries(image, pixels);
        }

        // TODO: Calculate where the 'horizontal' lines are, the finish-area
        // TODO: This also need to be added to the API
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
     * Using the image, calculate (and filter) where the left and right points of the track are.
     *
     * @param image
     * @param pixels
     * @return
     */
    private TrackBoundaries calculateTrackBoundaries(BufferedImage image, byte[] pixels) {

        Map<Integer, List<Integer>> scanlines = new HashMap<>();

        // For each row in the image, starting at the horizon (property setting)
        for(int y = WEBCAM_HORIZON; y < image.getHeight(); y++) {

            List<Integer> borders = new ArrayList<>();

            int consecutiveTapePixels = 0;

            // Scan the line, with overlapping pieces:
            for(int x = 0; x < image.getWidth(); x++) {

                int pixelLocation = calculatePixelLocation(image, y, x, 0, 0);
                int[] pixel = getBGR(pixels, pixelLocation);
                double tapeError = colorDifference(pixel, TAPE_COLOR);
                if (tapeError < TAPE_PIXEL_ERROR_THRESHOLD) {
                    consecutiveTapePixels++;
                } else {
                    // No more tape:
                    if(consecutiveTapePixels >= TAPE_WIDTH_THRESHOLD) {
                        borders.add(x - consecutiveTapePixels);
                        borders.add(x);
                    }
                    consecutiveTapePixels = 0;
                }
            }
            // Clean up running consecutive counts:
            if(--consecutiveTapePixels >= TAPE_WIDTH_THRESHOLD) {
                borders.add(image.getWidth() - 1 - consecutiveTapePixels);
            }

            // Try to determine the road from the analysed data:
            if(borders.size() > 0) {
                scanlines.put(y, borders);
            }
        }

        List<int[]> calculatedBoundaries = extractBoundaries(image, scanlines);

        //dumpDebugImage(image, calculatedBoundaries);

        TrackBoundaries newTrackBoundaries = new TrackBoundaries(calculatedBoundaries);
        return newTrackBoundaries;
    }

    /**
     * Input is all the scanlines of the image that contain at least one border
     * Now we assume we're on the track, so the middle of the bottom scanline *IS* track
     * From here we work our way up, find the piece of track in the line above between the previous two borders.
     *
     * @param image
     * @param scanlines
     * @return
     */
    private List<int[]> extractBoundaries(BufferedImage image, Map<Integer, List<Integer>> scanlines) {

        double middleOfTrack = (image.getWidth() - 1) / 2;

        List<int[]> boundaries = new ArrayList<>();
        boolean foundActualBorders = false;

        // Move from the bottom of the image up:
        for(int line = image.getHeight() - 1; line >= WEBCAM_HORIZON; line--) {

            if(scanlines.containsKey(line) || !foundActualBorders) {

                List<Integer> linePoints = scanlines.getOrDefault(line, new ArrayList<>());

                linePoints.add(0, 0);
                linePoints.add(image.getWidth() - 1);

                // Increase the points by TWO because we mark the start and end of boundary tape:
                for(int point = 0; point < linePoints.size() - 1; point +=2) {
                    int p1 = linePoints.get(point);
                    int p2 = linePoints.get(point+1);
                    // If this point is inside the middle of the track, register this as track:
                    if(p1 <= middleOfTrack && p2 > middleOfTrack) {
                        // (Slowly) adjust middle of track:
                        middleOfTrack = ((middleOfTrack * 2) + ((p1 + p2) / 2)) / 3;
                        boundaries.add(new int[] { line, p1, p2 });
                        break;
                    }
                }
                if(scanlines.containsKey(line)) {
                    foundActualBorders = true;
                }
            }
        }

        if(!foundActualBorders) {
            // Went throught the entire image without any clear boundaries?
            // Don't return anything
            boundaries.clear();
        }
        return boundaries;
    }


    /**
     * DEBUG OPTION: Output an image with some debug information points
     * Easy method to tweak the actual webcam images on-scene.
     *
     * @param trackBoundaries
     * @return
     */
    static int cnt = 0;
    private void dumpDebugImage(BufferedImage image, List<int[]> trackBoundaries) {
        BufferedImage tImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = (Graphics2D) tImage.getGraphics();
        g.drawImage(image, 0, 0, tImage.getWidth(), tImage.getHeight(), 0, 0, tImage.getWidth(), tImage.getHeight(), null);

        //Change color if there is a break in the detected area
        Color[] colors = new Color[] {Color.BLUE, Color.CYAN, Color.GREEN};
        int colorPtr = 0;
        int previousLineId = -1;
        for(int[] line:trackBoundaries) {
            g.setColor(colors[colorPtr]);
            //clamp values (detected track can lay outside of the image because of predictions)
            int lineId = line[0];
            int left = Math.max(0, line[1]);
            int right = Math.min(image.getWidth(), line[2]);
            g.drawLine(left + 1, lineId, right - 1, lineId);
            if(lineId != previousLineId-1) {
                colorPtr = (colorPtr+1)%3;
            }
            previousLineId = lineId;
        }

        try {
            ImageIO.write(tImage, "PNG", new File("output"+(cnt++)+".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Given x and y, and an offset, calculate the location in the array
     *
     * @param image
     * @param y
     * @param x
     * @param yOff
     * @param xOff
     * @return
     */
    private int calculatePixelLocation(BufferedImage image, int y, int x, int yOff, int xOff) {
        return ((y + yOff) * image.getWidth() * PIXEL_STEP) + ((x + xOff) * PIXEL_STEP);
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
                (p2[0]-p1[0])*(p2[0]-p1[0]) + (p2[1]-p1[1])*(p2[1]-p1[1]) + (p2[2]-p1[2])*(p2[2]-p1[2])
        );
    }

}
