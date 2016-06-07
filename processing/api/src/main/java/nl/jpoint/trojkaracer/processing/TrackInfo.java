package nl.jpoint.trojkaracer.processing;

public interface TrackInfo {
    long getTimestamp();

    boolean isStartSignRed();

    TrackBoundaries getBoundaries();
}
