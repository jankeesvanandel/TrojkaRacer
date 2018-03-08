package nl.jpoint.trojkaracer.car.domain.navigator;

import java.util.List;
import nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionHelper;
import nl.jpoint.trojkaracer.car.domain.computervision.Line;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

/**
 * The basic navigator.
 */
public class BasicNavigator implements Navigator {

    private final Flux<NavigationDirections> navigationDirectionsFlux;

    public BasicNavigator(final Flux<Mat> viewPublisher, final ComputerVisionHelper computerVisionHelper) {
        this.navigationDirectionsFlux = Flux.from(viewPublisher)
                .doOnNext(image -> {
                    Mat colorOfInterestImage = computerVisionHelper.filterToColor(image, new Scalar(15, 5, 25), new Scalar(180, 250, 250));
                    computerVisionHelper.writeImage(computerVisionHelper.addLanesToImage(computerVisionHelper.convertGrayScaleToBGRColor(colorOfInterestImage)), "lanes");
                })
                .map(computerVisionHelper::getLaneLines)
                .map(computerVisionHelper::filterLines)
                .map(this::navigateBetweenLines);
    }

    @Override
    public void subscribe(final Subscriber<? super NavigationDirections> s) {
        navigationDirectionsFlux.subscribe(s);
    }

    private NavigationDirections navigateBetweenLines(final List<Line> lines) {
        return NavigationDirections.of(0);
    }

}
