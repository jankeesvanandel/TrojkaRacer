package nl.jpoint.trojkaracer.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class ProcessingServiceImpl implements ProcessingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingServiceImpl.class);
    private final ImageProcessor imageProcessor;

    @Inject
    public ProcessingServiceImpl(final ImageProcessor imageProcessor) {
        this.imageProcessor = imageProcessor;
    }

    @Override
    public TrackInfo getTrackInfo() {
        LOGGER.debug("Retrieving track info");
        return new TrackInfoImpl(imageProcessor.getLatestTrackBoundaries(), imageProcessor.isWaitingForGreenLight());
    }
}
