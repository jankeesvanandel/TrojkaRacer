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
        Assert.assertEquals(185.0, location.getX(), 0.0);
        Assert.assertEquals(365.0, location.getY(), 0.0);
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

    private void fireAnalysisRunWithImage(String filename) {
        // Initialize with an image:
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(filename);
        imageReader.setImage(stream);
        // Fire a single analysis run:
        imageProcessor.run();
    }
}
