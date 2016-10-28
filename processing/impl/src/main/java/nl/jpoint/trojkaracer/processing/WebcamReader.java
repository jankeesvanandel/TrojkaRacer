package nl.jpoint.trojkaracer.processing;

import com.hopding.jrpicam.RPiCamera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.inject.Inject;

public class WebcamReader implements ImageReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebcamReader.class);

    private final RPiCamera rPiCamera;
    private final boolean pictureStoringEnabled;

    @Inject
    public WebcamReader(final RPiCamera rPiCamera, final boolean pictureStoringEnabled) {
        this.rPiCamera = rPiCamera;
        this.pictureStoringEnabled = pictureStoringEnabled;
    }

    @Override
    public BufferedImage fetchImage() {
        try {
            final BufferedImage bufferedImage;
            if (pictureStoringEnabled) {
                bufferedImage = ImageIO.read(rPiCamera.takeStill(String.format("image-%1$tY%1$tm%1$td-%1$tH:%1$tM:%1$tS.%1$tL.jpg", new Date())));
            } else {
                bufferedImage = rPiCamera.takeBufferedStill();
            }
            return bufferedImage;
        } catch (final IOException | InterruptedException e) {
            LOGGER.error("Failed to fetch the next image.", e);
            return null;
        }
    }
}
