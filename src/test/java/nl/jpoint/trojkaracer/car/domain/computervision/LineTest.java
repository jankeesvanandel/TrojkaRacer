package nl.jpoint.trojkaracer.car.domain.computervision;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Unit tests for the {@link Line} class.
 */
public class LineTest {

    @Test
    public void shouldReturnValidSlope() {
        assertThat(new Line(100d, 100d, 200d, 200d).getslope(), is(1.0));

        assertThat(new Line(100d, 100d, 200d, 100d).getslope(), is(0.0));
        assertThat(new Line(100d, 100d, 0d, 100d).getslope(), is(0.0));

        assertThat(new Line(100d, 100d, 100d, 200d).getslope(), is(Line.MAX_SLOPE));
        assertThat(new Line(100d, 100d, 100d, 0d).getslope(), is(Line.MAX_SLOPE));
    }
}
