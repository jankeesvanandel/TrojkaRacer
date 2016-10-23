package nl.jpoint.trojkaracer.hardwareinterface.adapter;

/**
 * Immutable value object that contains the three key pulse widths (in microseconds) for a PWM controlled Servo (or other hardware); the minimum pulse width,
 * the maximum pulse width and the neutral/middle pulse width.
 */
public class PWMValues {

    private static final int PERCENTAGE_DIVIDER = 100;

    private final int minimumPulseWidth;
    private final int maximumPulseWidth;
    private final int neutralPulseWidth;
    private final int positivePulseWidthRange;
    private final int negativePulseWidthRange;

    /**
     * Constructor that sets the minimum and maximum pulse widths and that sets the neutral pulse width to the average of these two.
     * @param minimumPulseWidth the minimum pulse width.
     * @param maximumPulseWidth the maximum pulse width.
     */
    public PWMValues(final int minimumPulseWidth, final int maximumPulseWidth) {
        this(minimumPulseWidth, (minimumPulseWidth + maximumPulseWidth) / 2, maximumPulseWidth);
    }

    /**
     * Constructor that sets the minimum, neutral and maximum pulse widths.
     * @param minimumPulseWidth the minimum pulse width.
     * @param neutralPulseWidth the neutral pulse width.
     * @param maximumPulseWidth the maximum pulse width.
     */
    public PWMValues(final int minimumPulseWidth, final int neutralPulseWidth, final int maximumPulseWidth) {
        if (maximumPulseWidth <= minimumPulseWidth || neutralPulseWidth < minimumPulseWidth || neutralPulseWidth > maximumPulseWidth) {
            throw new IllegalArgumentException("The provided pulse widths are not correct compared to each other.");
        }
        this.minimumPulseWidth = minimumPulseWidth;
        this.neutralPulseWidth = neutralPulseWidth;
        this.maximumPulseWidth = maximumPulseWidth;

        this.positivePulseWidthRange = maximumPulseWidth - neutralPulseWidth;
        this.negativePulseWidthRange = neutralPulseWidth - minimumPulseWidth;
    }

    /**
     * Returns the minimum pulse width.
     * @return the minimum pulse width.
     */
    public int getMinimumPulseWidth() {
        return minimumPulseWidth;
    }

    /**
     * Returns the maximum pulse width.
     * @return the maximum pulse width.
     */
    public int getMaximumPulseWidth() {
        return maximumPulseWidth;
    }

    /**
     * Returns the neutral pulse width.
     * @return the neutral pulse width.
     */
    public int getNeutralPulseWidth() {
        return neutralPulseWidth;
    }

    /**
     * Checks if the provided pulse width is outside the limits of this PWMValues; it is outside the limits if it is lower than the minimum pulse width or
     * higher than the maximum pulse width.
     * @return a boolean indicating if the pulse width is outside the limits.
     */
    public boolean isOutsideLimits(final int pulseWidth) {
        return pulseWidth < minimumPulseWidth || pulseWidth > maximumPulseWidth;
    }

    /**
     * Returns the pulse width indicated by the provided percentage; 0% means neutral position, a positive percentage means the percentage of the range
     * between neutral and maximum pulse widths and a negative percentage means the percentage of the range between the neutral and minimum pulse widths.
     * @param percentage the percentage to use.
     * @return the pulse width (in microseconds).
     */
    public int getPulseWidthForPercentage(final int percentage) {
        if (percentage > 100 || percentage < -100) {
            throw new IllegalArgumentException(String.format("Percentage of pulse width should be between -100%% and +100%% (was %s).", percentage));
        }

        final int pulseWidth;
        if (percentage >= 0) {
            pulseWidth = neutralPulseWidth + calculatePercentage(percentage, positivePulseWidthRange);
        } else {
            pulseWidth = neutralPulseWidth + calculatePercentage(percentage, negativePulseWidthRange);
        }
        return pulseWidth;
    }

    private int calculatePercentage(final int percentage, final int base) {
        return (base * percentage) / PERCENTAGE_DIVIDER;
    }

    /**
     * Returns the percentage of the pulse width indicated by the provided pulse width (in microseconds).
     * @param pulseWidth the pulse width (in microseconds).
     * @return a percentage (between -100 and +100).
     */
    public int getPercentageForPulseWidth(final int pulseWidth) {
        if (pulseWidth > maximumPulseWidth || pulseWidth < minimumPulseWidth) {
            throw new IllegalArgumentException(String.format("Pulse width should be between %s and %s (was %s).", minimumPulseWidth,
                    maximumPulseWidth, pulseWidth));
        }

        final int percentage;
        if (pulseWidth >= neutralPulseWidth) {
            percentage = (PERCENTAGE_DIVIDER * (pulseWidth - neutralPulseWidth)) / positivePulseWidthRange;
        } else {
            percentage = (-1 * PERCENTAGE_DIVIDER * (neutralPulseWidth - pulseWidth)) / negativePulseWidthRange;
        }
        return percentage;
    }

}
