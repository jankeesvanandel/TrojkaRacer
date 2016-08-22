package nl.jpoint.trojkaracer.processing;

import java.util.Collections;
import java.util.List;

public class TrackBoundaries {

    private final List<TrackHorizontalScan> scannedLines;

    public TrackBoundaries(final List<TrackHorizontalScan> scannedLines) {
        this.scannedLines = Collections.unmodifiableList(scannedLines);
    }

    /**
     * @return The track scan information, containing a list of scanned lines.
     * Every scanned line contains a left and right position.
     */
    public List<TrackHorizontalScan> getScannedLines() {
        return scannedLines;
    }
}
