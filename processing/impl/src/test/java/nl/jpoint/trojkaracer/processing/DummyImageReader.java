package nl.jpoint.trojkaracer.processing;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class DummyImageReader implements ImageReader {

    private InputStream dummyFile;

    public void setImage(InputStream dummyFile) {
        this.dummyFile = dummyFile;
    }

    @Override
    public BufferedImage fetchImage() {
        try {
            return ImageIO.read(dummyFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
