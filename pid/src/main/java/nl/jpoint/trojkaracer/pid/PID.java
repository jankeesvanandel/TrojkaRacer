package nl.jpoint.trojkaracer.pid;

import static nl.jpoint.trojkaracer.pid.PID.Direction.DIRECT;
import static nl.jpoint.trojkaracer.pid.PID.Direction.REVERSE;
import static nl.jpoint.trojkaracer.pid.PID.Mode.AUTOMATIC;

public final class PID {

    enum Mode {
        MANUAL, AUTOMATIC
    }

    enum Direction {
        DIRECT, REVERSE
    }

    private static final int NANO_TO_MILLI = 1000000;

    private long lastTime;
    private double input, output, setpoint;
    private double iTerm, lastInput;
    private double kp, ki, kd;
    private int sampleTime = 100;
    private double outMin, outMax;
    private Mode mode = AUTOMATIC;
    private Direction direction = DIRECT;

    public PID(double kp, double ki, double kd, final double min, final double max) {
        this.setTunings(kp, ki, kd);
        this.setOutputLimits(min, max);
    }

    public double getInput() {
        return this.input;
    }

    public double calculate(double newInput, double newSetpoint) {
        this.input = newInput;
        this.setpoint = newSetpoint;

        if (mode == AUTOMATIC) {
            long now = System.nanoTime() / NANO_TO_MILLI;
            int timeChange = (int) (now - lastTime);
            if (timeChange >= sampleTime) {
                // Compute all the working error variables
                double error = this.setpoint - this.input;
                iTerm += (ki * error);
                iTerm = between(outMin, iTerm, outMax);
                double dInput = (this.input - lastInput);

                //Compute PID output
                output = kp * error + iTerm - kd * dInput;
                output = between(outMin, output, outMax);

                //Remember some variables for next time
                lastInput = this.input;
                lastTime = now;
            }
        }
        return output;
    }

    public void setTunings(double kp, double ki, double kd) {
        if (kp < 0 || ki < 0 || kd < 0) return;

        double sampleTimeInSec = ((double) sampleTime) / 1000;
        this.kp = kp;
        this.ki = ki * sampleTimeInSec;
        this.kd = kd / sampleTimeInSec;

        if (direction == REVERSE) {
            this.kp = (0 - this.kp);
            this.ki = (0 - this.ki);
            this.kd = (0 - this.kd);
        }
    }

    public void setSampleTime(int newSampleTime) {
        if (newSampleTime > 0) {
            double ratio = (double) newSampleTime / (double) sampleTime;
            ki *= ratio;
            kd /= ratio;
            sampleTime = newSampleTime;
        }
    }

    public void setOutputLimits(double min, double max) {
        if (min > max) return;
        outMin = min;
        outMax = max;
    }

    public void setMode(Mode mode) {
        boolean newAuto = (mode == AUTOMATIC);
        if (newAuto && this.mode != AUTOMATIC) {
            // We just went from manual to auto
            initialize();
        }
        this.mode = mode;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    private void initialize() {
        lastInput = input;
        iTerm = between(outMin, output, outMax);
    }

    private static double between(double min, double value, double max) {
        return Math.max(min, Math.min(value, max));
    }

}