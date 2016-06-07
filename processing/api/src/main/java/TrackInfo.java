public interface TrackInfo {
    long getTimestamp();
    boolean isStartSignRed();
    TrackBoundaries getBoundaries();
}
