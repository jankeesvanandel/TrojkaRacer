package nl.jpoint.trojkaracer.car.domain.navigator;

import nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionHelper;
import nl.jpoint.trojkaracer.car.domain.computervision.Line;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.nio.file.Files;
import java.nio.file.Paths;
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
    }

    @Test
    @Ignore
    public void processAllImages() throws Exception {
        Files.list(Paths.get("src/test/resources/stills/")).forEach(path -> {
            process("src/test/resources/stills/" + path.getFileName().toString());
        });
    }

    @Test
    public void testAngles() throws Exception {
        Assert.assertEquals(-12, process("src/test/resources/stills/still-105147.439.jpg").getDegrees());
        Assert.assertEquals(-40, process("src/test/resources/stills/still-105302.608.jpg").getDegrees());
    }

    public NavigationDirections process(final String still) {
        // Load image:
        frame = Imgcodecs.imread(still);
        System.out.println(frame);

        // Get lines:
        final List<Line> lines = computerVisionHelper.filterLines(computerVisionHelper.getLaneLines(frame));

        // Put into processor:
        return lineProcessor.process(lines, computerVisionHelper, frame);
    }
}
