package nl.jpoint.trojkaracer.processing;

public final class TrackHorizontalScan {
    private final int leftCoordinate;
    private final int rightCoordinate;

    public TrackHorizontalScan(final int leftCoordinate, final int rightCoordinate) {
        this.leftCoordinate = leftCoordinate;
        this.rightCoordinate = rightCoordinate;
    }

    public int getLeftCoordinate() {
        return leftCoordinate;
    }

    public int getRightCoordinate() {
        return rightCoordinate;
    }
}
