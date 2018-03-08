package nl.jpoint.trojkaracer.car.domain.navigator;

import nl.jpoint.trojkaracer.car.domain.computervision.Line;

import java.util.List;

public class LineProcessor {

    /*
    Based on input lines, output the desired direction.
     */
    public NavigationDirections process(final List<Line> lines) {



        // Return value between -60 and 60
        return NavigationDirections.of(0);
    }
}
