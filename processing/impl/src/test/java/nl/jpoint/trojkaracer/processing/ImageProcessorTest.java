package nl.jpoint.trojkaracer.processing;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.io.InputStream;

public class ImageProcessorTest {

    private DummyImageReader imageReader;
    private ImageProcessor imageProcessor;

    @Before
    public void setup() {
        imageReader = new DummyImageReader();
        imageProcessor = new ImageProcessor(imageReader);
    }

    @Test
    public void initialRunDetectsTrafficLight() {

        // Analyse a image with a red light:
        fireAnalysisRunWithImage("trafficlight_red.jpg");

        // Get the location of the red light:
        Point2D location = imageProcessor.getTrafficLightLocation();

        // Verify the reddest area has been marked to contain the traffic light:
        Assert.assertEquals(365.0, location.getX(), 0.0);
        Assert.assertEquals(185.0, location.getY(), 0.0);
    }

    @Test
    public void testRaceStartDetection() {
        // Start with a red light:
        fireAnalysisRunWithImage("trafficlight_red.jpg");

        // Fire an image with a different red light (no start yet):
        fireAnalysisRunWithImage("trafficlight_red_diff.jpg");
        Assert.assertTrue(imageProcessor.isWaitingForGreenLight());

        // Fire an image with a green light, start the race:
        fireAnalysisRunWithImage("trafficlight_green.jpg");
        Assert.assertFalse(imageProcessor.isWaitingForGreenLight());
    }

    @Test
    public void testTrackBoundaryDetection1() {
        // Try to detect boundaries:
        fireAnalysisRunWithImage("road_test1.jpg");
        Assert.assertEquals(270, calculateTrackAverage(), 20.0);
    }

    @Test
    public void testTrackBoundaryDetection2() {
        // Try to detect boundaries:
        fireAnalysisRunWithImage("road_test2.jpg");
        Assert.assertEquals(340.0, calculateTrackAverage(), 20.0);
    }

    @Test
    public void testTrackBoundaryDetection3() {
        // Try to detect boundaries:
        fireAnalysisRunWithImage("road_test3.jpg");
        Assert.assertEquals(255.0, calculateTrackAverage(), 20.0);
    }

    @Test
    public void testTrackBoundaryDetection4() {
        // Try to detect boundaries:
        fireAnalysisRunWithImage("road_test4.jpg");
        Assert.assertEquals(300, calculateTrackAverage(), 20.0);
    }

    @Test
    public void testTrackBoundaryDetectionNoTrack() {
        // Try to detect boundaries:
        fireAnalysisRunWithImage("road_test_clean.jpg");
        TrackBoundaries boundaries = imageProcessor.getLatestTrackBoundaries();
        Assert.assertTrue(boundaries.getScannedLines().isEmpty());
    }

    private double calculateTrackAverage() {
        TrackBoundaries boundaries = imageProcessor.getLatestTrackBoundaries();

        // Calculate the average middle of the returned track:
        double[] avg = new double[2];
        for(int[] line : boundaries.getScannedLines()) {
            avg[0] += line[1];
            avg[1] += line[2];
        }
        avg[0] /= boundaries.getScannedLines().size();
        avg[1] /= boundaries.getScannedLines().size();

        return (avg[0] + avg[1]) / 2;
    }

    private void fireAnalysisRunWithImage(String filename) {
        // Initialize with an image:
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(filename);
        imageReader.setImage(stream);
        // Fire a single analysis run:
        imageProcessor.run();
    }
}
