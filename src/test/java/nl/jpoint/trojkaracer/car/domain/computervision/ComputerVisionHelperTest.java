package nl.jpoint.trojkaracer.car.domain.computervision;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the {@link ComputerVisionHelper} class. It provides some "scratch" testing methods, methods that do no real assertions, but write an image
 * to disk to verify correct workings of the {@link ComputerVisionHelper} class.
 */
public class ComputerVisionHelperTest {

    private static VideoCapture camera;

    private ComputerVisionHelper computerVisionHelper;
    private Mat frame;

    @BeforeClass
    public static void cameraInit() throws InterruptedException {
        System.out.println(System.getProperty("java.library.path"));
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        camera = new VideoCapture(0);

        // Sleeping for 100 ms to make sure the camera is initialized correctly
        Thread.sleep(100);
    }

    @Before
    public void setUp() {
        computerVisionHelper = new ComputerVisionHelper(".");

//        frame = new Mat();
//        camera.read(frame);
        frame = Imgcodecs.imread("src/test/resources/stills/still-105147.439.jpg");
    }

    @Test
    public void testLaneDetection() {
        final List<Line> lines = computerVisionHelper.getLaneLines(frame);
        computerVisionHelper.filterLines(lines)
                .forEach(line -> line.draw(frame, new Scalar(0, 0, 255), 3));

        computerVisionHelper.writeImage(frame, "test-lanedetection");
    }

    @Test
    public void testEdgeDetection() {
        final Mat edgesImage =
                computerVisionHelper.cannyEdgeDetection(
                        computerVisionHelper.removeNoise(
                                computerVisionHelper.convertBGRToGrayScale(frame)));
        computerVisionHelper.writeImage(edgesImage, "test-edgedetection");
    }

    @Test
    public void testRegionOfInterest() {
        final List<MatOfPoint> vertices = new ArrayList<>();
        vertices.add(new MatOfPoint(new Point(100, 100), new Point(100, 400), new Point(400, 400), new Point(400, 100)));

        final Mat result = computerVisionHelper.applyMask(frame, computerVisionHelper.getRegionOfInterestMask(frame, vertices));
        computerVisionHelper.writeImage(result, "test-roi");
    }

    @Test
    public void testColorMask() {
        computerVisionHelper.writeImage(frame, "test-source");
        final Mat result = computerVisionHelper.filterToColor(frame, 25);
        computerVisionHelper.writeImage(result, "test-color-mask");
    }

    @Test
    public void shouldConvertRGBToHueValue() {
        assertThat(computerVisionHelper.getHueForRGB(0, 255, 0), is(60));
        assertThat(computerVisionHelper.getHueForRGB(220, 218, 231), is(125));
    }
}

