package nl.jpoint.trojkaracer.processing;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link ImageDirectoryReader} class.
 */
public class ImageDirectoryReaderTest {

    private static final Path RESOURCES_PATH = Paths.get("src/test/resources");

    @Test
    public void shouldReturnCorrectFileForFileNameComparator() throws IOException {
        assertEquals(1, ImageDirectoryReader.FILE_NAME_COMPARATOR.compare(RESOURCES_PATH.resolve("road_test2.jpg").toFile(),
                RESOURCES_PATH.resolve("road_test1.jpg").toFile()));
    }

    @Test
    public void shouldReturnCorrectFileForLastModifiedComparator() {
        assertEquals(1, ImageDirectoryReader.FILE_LASTMODIFIED_DATE_COMPARATOR.compare(RESOURCES_PATH.resolve("fisheye.jpg").toFile(),
                RESOURCES_PATH.resolve("road_test1.jpg").toFile()));
    }
}
