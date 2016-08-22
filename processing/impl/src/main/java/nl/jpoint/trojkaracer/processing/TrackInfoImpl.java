package nl.jpoint.trojkaracer.processing;

public class TrackInfoImpl implements TrackInfo {

    private long timestamp;
    private boolean waitingForStart;
    private TrackBoundaries trackBoundaries;

    public TrackInfoImpl(TrackBoundaries trackBoundaries, boolean waitingForStart) {
        this.trackBoundaries = trackBoundaries;
        this.waitingForStart = waitingForStart;
        this.timestamp = System.nanoTime();
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean isStartSignRed() {
        return waitingForStart;
    }

    @Override
    public TrackBoundaries getBoundaries() {
        return trackBoundaries;
    }
}
