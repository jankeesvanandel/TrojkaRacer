package nl.jpoint.trojkaracer.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Predicate;

import javax.imageio.ImageIO;
import javax.inject.Inject;

/**
 * Simply reads a directory
 */
public class ImageDirectoryReader implements ImageReader {

    public static final Comparator<File> FILE_NAME_COMPARATOR = (file1, file2) -> file1.getName().compareTo(file2.getName());
    public static final Comparator<File> FILE_LASTMODIFIED_DATE_COMPARATOR = (file1, file2) -> Long.compare(file1.lastModified(), file2.lastModified());

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageDirectoryReader.class);

    private final Path imageDirectory;
    private final Predicate<String> fileNamePredicate;
    private final Comparator<File> fileComparator;

    @Inject
    public ImageDirectoryReader(final Path imageDirectory, final Predicate<String> fileNamePredicate, final Comparator<File> fileComparator) {
        this.imageDirectory = imageDirectory;
        this.fileNamePredicate = fileNamePredicate;
        this.fileComparator = fileComparator;
    }

    @Override
    public BufferedImage fetchImage() {
        BufferedImage bufferedImage;
        try {
            final File imageFile = Files.list(imageDirectory)
                    .map(Path::toFile)
                    .filter(file -> fileNamePredicate.test(file.getName()))
                    .max(fileComparator)
                    .orElse(null);
            if (imageFile != null) {
                LOGGER.debug("Reading image from file {}", imageFile);
                bufferedImage = ImageIO.read(imageFile);
            } else {
                LOGGER.debug("No image file found");
                bufferedImage = null;
            }
        } catch (final IOException ioe) {
            LOGGER.error("Failed to read correct image file", ioe);
            bufferedImage = null;
        }
        return bufferedImage;
    }

}
