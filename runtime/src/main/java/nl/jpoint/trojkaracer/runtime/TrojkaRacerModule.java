package nl.jpoint.trojkaracer.runtime;

import com.hopding.jrpicam.RPiCamera;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;

import nl.jpoint.trojkaracer.ai.AIService;
import nl.jpoint.trojkaracer.ai.AIServiceImpl;
import nl.jpoint.trojkaracer.hardwareinterface.ArduinoHardwareControllerModule;
import nl.jpoint.trojkaracer.processing.ImageProcessor;
import nl.jpoint.trojkaracer.processing.ImageReader;
import nl.jpoint.trojkaracer.processing.ProcessingService;
import nl.jpoint.trojkaracer.processing.ProcessingServiceImpl;
import nl.jpoint.trojkaracer.processing.WebcamReader;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
    private static final int IMAGE_WIDTH = 500;
    private static final int IMAGE_HEIGHT = 500;
    private static final int BRIGHTNESS = 50;

    @Provides
    RPiCamera provideRPICamera() {
        try {
            final RPiCamera rPiCamera = new RPiCamera(DEFAULT_IMAGE_STORING_DIRECTORY);
            rPiCamera.setWidth(IMAGE_WIDTH);
            rPiCamera.setHeight(IMAGE_HEIGHT);
            rPiCamera.setBrightness(BRIGHTNESS);
            return rPiCamera;
        } catch (final FailedToRunRaspistillException e) {
            LOGGER.error("Failed to create the camera instance", e);
            return null;
        }
    }

    @Provides
    ImageReader provideImageReader(final RPiCamera rPiCamera) {
        return new WebcamReader(rPiCamera, STORE_IMAGES);
    }

    @Provides
    ImageProcessor provideImageProcessor(final ImageReader imageReader) {
        return new ImageProcessor(imageReader);
    }

    @Provides
    ProcessingService provideProcessingService(final ImageProcessor imageProcessor) {
        return new ProcessingServiceImpl(imageProcessor);
    }

    @Provides
    AIService provideAIService(final ProcessingService processingService) {
        return new AIServiceImpl(processingService);
    }

    @Provides
    ScheduledExecutorService provideScheduledExecutorService() {
        return Executors.newScheduledThreadPool(2);
    }
}
