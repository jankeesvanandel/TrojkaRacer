package nl.jpoint.trojkaracer.processing;

import javax.inject.Inject;

public class ProcessingServiceImpl implements ProcessingService {

    private final ImageProcessor imageProcessor;

    @Inject
    public ProcessingServiceImpl(final ImageProcessor imageProcessor) {
        this.imageProcessor = imageProcessor;
    }

    @Override
    public TrackInfo getTrackInfo() {
        return new TrackInfoImpl(imageProcessor.getLatestTrackBoundaries(), imageProcessor.isWaitingForGreenLight());
    }
}
