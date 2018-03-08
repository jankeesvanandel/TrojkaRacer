package nl.jpoint.trojkaracer.car.domain.navigator;

import nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionHelper;
import nl.jpoint.trojkaracer.car.domain.computervision.Line;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.List;

public class LineProcessorTest {

    private LineProcessor lineProcessor = new LineProcessor();

    private ComputerVisionHelper computerVisionHelper;
    private Mat frame;

    @BeforeClass
    public static void cameraInit() throws InterruptedException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // Sleeping for 100 ms to make sure the camera is initialized correctly
        Thread.sleep(100);
    }

    @Before
    public void setUp() {
        computerVisionHelper = new ComputerVisionHelper(".");
        frame = Imgcodecs.imread("src/test/resources/stills/still-105147.439.jpg");
    }

    @Test
    public void processImages() {
        process("src/test/resources/stills/still-105147.439.jpg");
    }

    public NavigationDirections process(final String still) {
        // Load image:
        frame = Imgcodecs.imread(still);
        System.out.println(frame);

        // Get lines:
        final List<Line> lines = computerVisionHelper.filterLines(computerVisionHelper.getLaneLines(frame));

        // Put into processor:
        return lineProcessor.process(lines);
    }
}
