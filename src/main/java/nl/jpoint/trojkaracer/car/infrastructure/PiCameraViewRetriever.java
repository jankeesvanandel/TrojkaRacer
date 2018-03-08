package nl.jpoint.trojkaracer.car.infrastructure;

import nl.jpoint.trojkaracer.car.domain.ViewRetriever;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.springframework.stereotype.Component;

/**
 * Implementation of the {@link nl.jpoint.trojkaracer.car.domain.ViewRetriever}
 */
@Component
public class PiCameraViewRetriever implements ViewRetriever {

    private final VideoCapture camera;

    public PiCameraViewRetriever(final VideoCapture camera) {
        this.camera = camera;
    }

    @Override
    public Mat getView() {
        final Mat view = new Mat();
        camera.read(view);
        Core.flip(view, view, -1);

        return view;
    }
}
