package nl.jpoint.trojkaracer.processing;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessor implements Runnable {

    // ----------------------------------------------------------------
    // TODO: These properties should come from properties-project, injected here?
    // Square area we're detecting as containing traffic light:
    private static final int LIGHT_SIZE = 20;

    // Amount of consecutive pixels to make up a border-tape:
    private static final int TAPE_WIDTH_THRESHOLD = 3;
    // Mean sqrt color error between TAPE_COLOR and camera
    private static final double TAPE_PIXEL_ERROR_THRESHOLD = 100;

    // Color of the boundaries tape (in BGR):
    private static final int[] TAPE_COLOR = new int[] {20 ,20, 20};
    // Color that kind of matches the red light (in BGR):
    private static final int[] RED_LIGHT_COLOR = new int[] {10 ,10, 155};
    // Color that matches the green light (in BGR):
    private static final int[] GREEN_LIGHT_COLOR = new int[] {67 ,143, 74};
    // Horizon of the (flat) road, don't look above this line:
    private static final int WEBCAM_HORIZON = 120;

    // When filtering the outliers from the track data, use this threshold:
    private static final int NOISE_THRESHOLD = 20;
    // When filtering the outliers use this much points to calculate the average:
    private static final int NOISE_POINTS_AVERAGE = 7;
    // ----------------------------------------------------------------

    // Looping in pixel arrays the step is 3 (B/G/R)
    private static final int PIXEL_STEP = 3;

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

        // Fetch image from webcam:
        BufferedImage image = imageReader.fetchImage();

        // Analyze image:

        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        if(trafficLightLocation == null) {
            // We're starting, no traffic light has been found yet:
            trafficLightLocation = findRedTrafficLightLocation(image, pixels);
            // Calculate the track once (when we start we have a track):
            trackBoundaries = calculateTrackBoundaries(image, pixels);
        } else if(waitingForGreenLight) {
            // We're waiting for the light to turn green, check the color:
            if(trafficLightTurnedGreen(image, pixels)) {
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

        List<int[]> scannedLines = new ArrayList<>();

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
                        borders.add(x - (consecutiveTapePixels / 2));
                    }
                    consecutiveTapePixels = 0;
                }
            }
            // Clean up running consecutive counts:
            if(--consecutiveTapePixels >= TAPE_WIDTH_THRESHOLD) {
                borders.add(image.getWidth() - 1 - (consecutiveTapePixels / 2));
            }

            // Try to determine the road from the analysed data:
            int[] scanline = new int[3];
            scanline[0] = y;
            if(borders.size() > 1) {
                int maxLength = 0;
                for(int borderPtr = 0; borderPtr < borders.size() - 1; borderPtr++) {
                    int length = borders.get(borderPtr + 1) - borders.get(borderPtr);
                    if(length > maxLength) {
                        maxLength = length;
                        scanline[1] = borders.get(borderPtr);
                        scanline[2] = borders.get(borderPtr + 1);
                    }
                }
                scannedLines.add(scanline);
            }
        }

        List<int[]> filteredLines = filterOutliers(scannedLines);

        //dumpDebugImage(image, scannedLines, filteredLines);

        TrackBoundaries newTrackBoundaries = new TrackBoundaries(filteredLines);
        return newTrackBoundaries;
    }

    /**
     * DEBUG OPTION: Output an image with some debug information points
     * Easy method to tweak the actual webcam images on-scene.
     *
     * @param scannedLines
     * @return
     */
    private void dumpDebugImage(BufferedImage image, List<int[]> scannedLines, List<int[]> filteredLines) {
        BufferedImage tImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = (Graphics2D) tImage.getGraphics();
        g.drawImage(image, 0, 0, tImage.getWidth(), tImage.getHeight(), 0, 0, tImage.getWidth(), tImage.getHeight(), null);
        for(int[] line:scannedLines) {
            tImage.setRGB(line[1], line[0], Color.RED.getRGB());
            tImage.setRGB(line[2], line[0], Color.RED.getRGB());
        }
        for(int[] line:filteredLines) {
            tImage.setRGB(line[1], line[0], Color.BLUE.getRGB());
            tImage.setRGB(line[2], line[0], Color.BLUE.getRGB());
        }

        try {
            ImageIO.write(tImage, "PNG", new File("output.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * With the fast line-by-line road detection algorithm there are a lot of outliers.
     * Using a simple moving average we can remove most of the outliers quickly.
     *
     * @param scannedLines
     * @return
     */
    private List<int[]> filterOutliers(List<int[]> scannedLines) {
        List<int[]> filteredLines = new ArrayList<>();
        for(int line = 0; line < scannedLines.size(); line++) {
            // Take some values before and after our current line:
            int startValue = line - (NOISE_POINTS_AVERAGE / 2);
            int endValue = line + (NOISE_POINTS_AVERAGE / 2);

            // Clamp to avoid AOOBExceptions:
            if(startValue < 0) {
                endValue -= startValue;
                startValue = 0;
            } else if (endValue > scannedLines.size()) {
                startValue -= (endValue - scannedLines.size());
                endValue = scannedLines.size();
            }

            // Calculate the average point of the lines before and after:
            int[] avg = new int[2];
            for(int cLine = startValue; cLine < endValue; cLine++) {
                int[] cL = scannedLines.get(cLine);
                avg[0] += cL[1];
                avg[1] += cL[2];
            }
            avg[0] /= (endValue-startValue);
            avg[1] /= (endValue-startValue);

            // If this is not an outlier, include it in the output:
            int noise = Math.abs(scannedLines.get(line)[1] - avg[0]) + Math.abs(scannedLines.get(line)[2] - avg[1]);
            if(noise < NOISE_THRESHOLD) {
                filteredLines.add(scannedLines.get(line));
            }
        }
        return filteredLines;
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
