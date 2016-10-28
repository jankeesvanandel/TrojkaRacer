package nl.jpoint.trojkaracer.runtime;

import com.hopding.jrpicam.RPiCamera;
import com.hopding.jrpicam.enums.AWB;
import com.hopding.jrpicam.enums.DRC;
import com.hopding.jrpicam.enums.Encoding;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;

import nl.jpoint.trojkaracer.ai.AIService;
import nl.jpoint.trojkaracer.ai.AIServiceImpl;
import nl.jpoint.trojkaracer.ai.HardWiredAIService;
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
    private static final boolean STORE_IMAGES = false;
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
        return new WebcamReader(rPiCamera, STORE_IMAGES);
    }

    @Provides
    @Singleton
    ImageProcessor provideImageProcessor(final ImageReader imageReader) {
//        return new ImageProcessor(imageReader);
        return new ImageProcessor(imageReader);
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
    @Singleton
    ScheduledExecutorService provideScheduledExecutorService() {
        return Executors.newScheduledThreadPool(2);
    }
}
