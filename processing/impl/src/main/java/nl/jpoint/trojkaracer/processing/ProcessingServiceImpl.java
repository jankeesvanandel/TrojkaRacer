package nl.jpoint.trojkaracer.processing;

import javax.inject.Inject;

public class ProcessingServiceImpl implements ProcessingService {

    @Inject
    ImageProcessor imageProcessor;

    @Override
    public TrackInfo getTrackInfo() {
        return new TrackInfoImpl(imageProcessor.getLatestTrackBoundaries(), imageProcessor.isWaitingForGreenLight());
    }
}
