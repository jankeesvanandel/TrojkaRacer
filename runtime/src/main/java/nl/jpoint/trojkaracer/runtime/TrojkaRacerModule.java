package nl.jpoint.trojkaracer.runtime;

import nl.jpoint.trojkaracer.ai.AIService;
import nl.jpoint.trojkaracer.ai.AIServiceImpl;
import nl.jpoint.trojkaracer.hardwareinterface.ArduinoHardwareControllerModule;
import nl.jpoint.trojkaracer.processing.ImageProcessor;
import nl.jpoint.trojkaracer.processing.ImageReader;
import nl.jpoint.trojkaracer.processing.ProcessingService;
import nl.jpoint.trojkaracer.processing.ProcessingServiceImpl;
import nl.jpoint.trojkaracer.processing.WebcamReader;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import dagger.Module;
import dagger.Provides;

/**
 * The Dagger Dependency Injection Module; contains the wiring of the object graph.
 */
@Module(injects = TrojkaRacerRunner.class, includes = { ArduinoHardwareControllerModule.class })
public class TrojkaRacerModule {

    @Provides
    ImageReader provideImageReader() {
        return new WebcamReader();
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
        return Executors.newSingleThreadScheduledExecutor();
    }
}
