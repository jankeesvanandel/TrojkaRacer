package nl.jpoint.trojkaracer.hardwareinterface.adapter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link PWMValues} class.
 */
public class PWMValuesTest {

    private final PWMValues pwmValues = new PWMValues(500, 1000, 2000);

    @Test
    public void shouldCreateWithAverageNeutral() {
        final PWMValues pwmValuesNew = new PWMValues(0, 500);
        assertEquals(250, pwmValuesNew.getNeutralPulseWidth());
    }

    @Test
    public void shouldReportOutOfLimits() {
        assertTrue(pwmValues.isOutsideLimits(0));
        assertTrue(pwmValues.isOutsideLimits(-750));
        assertTrue(pwmValues.isOutsideLimits(2100));

        assertFalse(pwmValues.isOutsideLimits(500));
        assertFalse(pwmValues.isOutsideLimits(750));
        assertFalse(pwmValues.isOutsideLimits(1000));
        assertFalse(pwmValues.isOutsideLimits(1400));
        assertFalse(pwmValues.isOutsideLimits(2000));
    }

    @Test
    public void shouldCalculateCorrectPercentagesForPulseWidths() {
        assertEquals(-100, pwmValues.getPercentageForPulseWidth(500));
        assertEquals(0, pwmValues.getPercentageForPulseWidth(1000));
        assertEquals(100, pwmValues.getPercentageForPulseWidth(2000));

        assertEquals(-25, pwmValues.getPercentageForPulseWidth(875));
        assertEquals(-80, pwmValues.getPercentageForPulseWidth(600));

        assertEquals(25, pwmValues.getPercentageForPulseWidth(1250));
        assertEquals(60, pwmValues.getPercentageForPulseWidth(1600));
        assertEquals(90, pwmValues.getPercentageForPulseWidth(1900));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForTooLowPulseWidth() {
        pwmValues.getPercentageForPulseWidth(490);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForTooHighPulseWidth() {
        pwmValues.getPercentageForPulseWidth(2010);
    }

    @Test
    public void shouldCalculateCorrectPulseWidthsForPercentages() {
        assertEquals(500, pwmValues.getPulseWidthForPercentage(-100));
        assertEquals(1000, pwmValues.getPulseWidthForPercentage(0));
        assertEquals(2000, pwmValues.getPulseWidthForPercentage(100));

        assertEquals(875, pwmValues.getPulseWidthForPercentage(-25));
        assertEquals(600, pwmValues.getPulseWidthForPercentage(-80));

        assertEquals(1250, pwmValues.getPulseWidthForPercentage(25));
        assertEquals(1600, pwmValues.getPulseWidthForPercentage(60));
        assertEquals(1900, pwmValues.getPulseWidthForPercentage(90));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForTooLowPercentage() {
        pwmValues.getPulseWidthForPercentage(-110);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForTooHighPercentage() {
        pwmValues.getPulseWidthForPercentage(110);
    }
}
