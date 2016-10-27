package nl.jpoint.trojkaracer.processing;

import com.hopding.jrpicam.RPiCamera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.inject.Inject;

public class WebcamReader implements ImageReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebcamReader.class);

    private final RPiCamera rPiCamera;

    @Inject
    public WebcamReader(final RPiCamera rPiCamera) {
        this.rPiCamera = rPiCamera;
    }

    @Override
    public BufferedImage fetchImage() {
        try {
            return rPiCamera.takeBufferedStill();
        } catch (final IOException | InterruptedException e) {
            LOGGER.error("Failed to fetch the next image.", e);
            return null;
        }
    }
}
