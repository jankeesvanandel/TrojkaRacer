package nl.jpoint.trojkaracer.car.domain.starter;

import java.lang.invoke.MethodHandles;
import nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionHelper;
import org.opencv.core.Mat;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

/**
 * Implementation of the {@link Starter} interface; this implementation sends a signal when it has detected a red light and has seen it
 * switched off.
 */
public class RedLightStarter implements Starter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int RED_LIGHT_THRESHOLD = 20;

    private final Publisher<Boolean> startPublisher;

    private int previousNrOfPixels = 0;
    private int currentNrOfPixels = 0;

    public RedLightStarter(final Flux<Mat> viewPublisher, final ComputerVisionHelper computerVisionHelper) {
        final int redHue = computerVisionHelper.getHueForRGB(255, 0, 0);

        startPublisher = Flux.from(viewPublisher)
                .map(image -> computerVisionHelper.filterToColor(image, redHue))
                .map(computerVisionHelper::findAmountOfNonBlackPixels)
                .filter(this::redLightWentOff)
                .map(image -> Boolean.TRUE);
    }

    private boolean redLightWentOff(final int nonBlackPixels) {
        if (currentNrOfPixels == 0 && previousNrOfPixels == 0) {
            currentNrOfPixels = nonBlackPixels;
        }

        previousNrOfPixels = currentNrOfPixels;
        currentNrOfPixels = nonBlackPixels;
        if (Math.abs(previousNrOfPixels - currentNrOfPixels) > RED_LIGHT_THRESHOLD / 5) {
            LOGGER.debug("{} detected red light changes with current '{}' black pixels versus '{}' previously where the threshold is '{}'.",
                    getClass().getSimpleName(), currentNrOfPixels, previousNrOfPixels, RED_LIGHT_THRESHOLD);
        }
        return previousNrOfPixels - currentNrOfPixels > RED_LIGHT_THRESHOLD;
    }

    @Override
    public void subscribe(final Subscriber<? super Boolean> s) {
        startPublisher.subscribe(s);
    }
}
