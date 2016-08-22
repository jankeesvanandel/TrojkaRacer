package nl.jpoint.trojkaracer.processing;

import java.util.Collections;
import java.util.List;

public class TrackBoundaries {

    private final List<int[]> scannedLines;

    public TrackBoundaries(List<int[]> scannedLines) {
        this.scannedLines = Collections.unmodifiableList(scannedLines);
    }

    /**
     * @return The track scan information, containing a list of scanned lines.
     * Every scanned line contains a left and right position.
     */
    public List<int[]> getScannedLines() {
        return Collections.unmodifiableList(scannedLines);
    }
}
