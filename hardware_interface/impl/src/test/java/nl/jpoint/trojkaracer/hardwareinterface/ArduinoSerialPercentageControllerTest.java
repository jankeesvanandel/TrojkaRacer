package nl.jpoint.trojkaracer.hardwareinterface;

import nl.jpoint.trojkaracer.hardwareinterface.adapter.ArduinoSerialCommandAdapter;
import nl.jpoint.trojkaracer.hardwareinterface.adapter.PWMValues;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link ArduinoSerialPercentageController} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ArduinoSerialPercentageControllerTest {

    private static final PWMValues SPEED_PWM_VALUES = new PWMValues(500, 1000, 2000);
    private static final PWMValues DIRECTION_PWM_VALUES = new PWMValues(10, 110, 210);

    @Mock
    private ArduinoSerialCommandAdapter arduinoSerialCommandAdapter;

    private ArduinoSerialPercentageController arduinoSerialPercentageController;

    @Before
    public void init() {
        arduinoSerialPercentageController = new ArduinoSerialPercentageController(arduinoSerialCommandAdapter, SPEED_PWM_VALUES, DIRECTION_PWM_VALUES);
        when(arduinoSerialCommandAdapter.setSpeed(anyInt())).thenReturn(1000);
        when(arduinoSerialCommandAdapter.setDirection(anyInt())).thenReturn(110);
    }

    @Test
    public void shouldCallAdapterWithPulseWidthForSpeed() {
        arduinoSerialPercentageController.setSpeed(0);

        verify(arduinoSerialCommandAdapter, times(1)).setSpeed(1000);
    }

    @Test
    public void shouldCallAdapterWithPulseWidthForDirection() {
        arduinoSerialPercentageController.setDirection(0);

        verify(arduinoSerialCommandAdapter, times(1)).setDirection(110);
    }

    @Test
    public void shouldStopAdapterWhenStopped() {
        arduinoSerialCommandAdapter.stop();

        verify(arduinoSerialCommandAdapter, times(1)).stop();
    }

}
