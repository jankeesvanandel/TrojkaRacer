package nl.jpoint.trojkaracer.runtime;

import com.hopding.jrpicam.RPiCamera;
import com.hopding.jrpicam.enums.AWB;
import com.hopding.jrpicam.enums.DRC;
import com.hopding.jrpicam.enums.Encoding;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;

import nl.jpoint.trojkaracer.DeadmanSwitch;
import nl.jpoint.trojkaracer.ai.AIService;
import nl.jpoint.trojkaracer.ai.AIServiceImpl;
import nl.jpoint.trojkaracer.ai.HardWiredAIService;
import nl.jpoint.trojkaracer.hardwareinterface.ArduinoHardwareControllerModule;
import nl.jpoint.trojkaracer.hardwareinterface.DirectionController;
import nl.jpoint.trojkaracer.hardwareinterface.SpeedController;
import nl.jpoint.trojkaracer.pid.Killable;
import nl.jpoint.trojkaracer.processing.ImageDirectoryReader;
import nl.jpoint.trojkaracer.processing.ImageProcessor;
import nl.jpoint.trojkaracer.processing.ImageReader;
import nl.jpoint.trojkaracer.processing.ProcessingService;
import nl.jpoint.trojkaracer.processing.ProcessingServiceImpl;
import nl.jpoint.trojkaracer.processing.WebcamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * The Dagger Dependency Injection Module; contains the wiring of the object graph.
 */
@Module(injects = TrojkaRacerRunner.class, includes = { ArduinoHardwareControllerModule.class })
public class TrojkaRacerModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrojkaRacerModule.class);
    private static final String DEFAULT_IMAGE_STORING_DIRECTORY = "/home/pi/Pictures";
    private static final boolean STORE_IMAGES = true;
    private static final int IMAGE_WIDTH = 640;
    private static final int IMAGE_HEIGHT = 480;
    private static final int BRIGHTNESS = 50;

    @Provides
    @Singleton
    RPiCamera provideRPICamera() {
        try {
            final RPiCamera rPiCamera = new RPiCamera(DEFAULT_IMAGE_STORING_DIRECTORY);
            rPiCamera.setToDefaults();
            rPiCamera.setWidth(IMAGE_WIDTH);
            rPiCamera.setHeight(IMAGE_HEIGHT);
//            rPiCamera.setBrightness(BRIGHTNESS);
            rPiCamera.setFullPreviewOff();
//            rPiCamera.setDRC(DRC.OFF);
//            rPiCamera.setAWB(AWB.OFF);
            rPiCamera.setTimeout(1);
            return rPiCamera;
        } catch (final FailedToRunRaspistillException e) {
            LOGGER.error("Failed to create the camera instance", e);
            return null;
        }
    }

    @Provides
    @Singleton
    ImageReader provideImageReader(final RPiCamera rPiCamera) {
//        return new WebcamReader(rPiCamera, STORE_IMAGES);
        return new ImageDirectoryReader(Paths.get("/home/pi/Pictures"), fileName -> fileName.endsWith(".jpg"),
                ImageDirectoryReader.FILE_LASTMODIFIED_DATE_COMPARATOR);
    }

    @Provides
    @Singleton
    ImageProcessor provideImageProcessor(final ImageReader imageReader) {
        return new ImageProcessor(imageReader).withDebugImageOutputPath("/home/pi/debug-images/");
//        return new ImageProcessor(imageReader).withDebugImageOutputPath("/home/pi/debug-images/").withNoEyeForTrafficLights();
    }

    @Provides
    @Singleton
    ProcessingService provideProcessingService(final ImageProcessor imageProcessor) {
        return new ProcessingServiceImpl(imageProcessor);
    }

    @Provides
    @Singleton
    AIService provideAIService(final ProcessingService processingService) {
//        return new HardWiredAIService();
        return new AIServiceImpl(processingService);
    }

    @Provides
    DeadmanSwitch provideDeadmanSwitch(final Killable killable, final InetAddress address){
        return new DeadmanSwitch(killable, address);
    }

    @Provides
    Killable provideKillable(final TrojkaRacerMainLoop trojkaRacerMainLoop) {
        return trojkaRacerMainLoop;
    }

    @Provides
    InetAddress provideInetAdress(@Named("deadmanswitch.host") final String deadmanHost){
        try {
            return InetAddress.getByName(deadmanHost);
        } catch (UnknownHostException e) {
            LOGGER.error("Error finding host for deadman switch", e);
        }
        return null;
    }

    @Provides
    @Named("deadmanswitch.host")
    String provideDeadmanSwitchHost(){
        return "10.0.1.2";
    }

    @Provides
    @Singleton
    ScheduledExecutorService provideScheduledExecutorService() {
        return Executors.newScheduledThreadPool(2);
    }
}
