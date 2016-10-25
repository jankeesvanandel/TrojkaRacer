package nl.jpoint.trojkaracer.hardwareinterface.adapter;

import nl.jpoint.trojkaracer.hardwareinterface.HardwareInterfaceException;
import nl.jpoint.trojkaracer.hardwareinterface.PWMPulseOutsideLimitsException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import jssc.SerialPort;
import jssc.SerialPortException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link ArduinoSerialCommandAdapter} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ArduinoSerialCommandAdapterTest {

    private static final PWMValues SPEED_PWM_VALUES = new PWMValues(500, 1000, 2000);
    private static final PWMValues DIRECTION_PWM_VALUES = new PWMValues(10, 110, 210);

    @Mock
    private SerialPort serialPort;

    private ArduinoSerialCommandAdapter arduinoSerialCommandAdapter;

    @Before
    public void init() throws SerialPortException {
        when(serialPort.readString()).thenReturn("\n");
        arduinoSerialCommandAdapter = new ArduinoSerialCommandAdapter(serialPort, SPEED_PWM_VALUES, DIRECTION_PWM_VALUES);
    }

    @Test
    public void shouldSendCommandForSettingSpeed() throws SerialPortException {
        arduinoSerialCommandAdapter.setSpeed(1100);
        verify(serialPort, times(1)).writeString(eq("THR1100\n"));
    }

    @Test(expected = PWMPulseOutsideLimitsException.class)
    public void shouldThrowExceptionForSettingIncorrectSpeed() {
        arduinoSerialCommandAdapter.setSpeed(490);
    }

    @Test
    public void shouldSendCommandForSettingDirection() throws SerialPortException {
        arduinoSerialCommandAdapter.setDirection(80);
        verify(serialPort, times(1)).writeString(eq("STE80\n"));
    }

    @Test(expected = PWMPulseOutsideLimitsException.class)
    public void shouldThrowExceptionForSettingIncorrectDirection() {
        arduinoSerialCommandAdapter.setDirection(0);
    }

    @Test(expected = HardwareInterfaceException.class)
    public void shouldWrapSerialPortException() throws SerialPortException {
        when(serialPort.writeString(anyString())).thenThrow(new SerialPortException("test", "test", "test"));

        arduinoSerialCommandAdapter.setSpeed(1100);
    }

    @Test
    public void shouldNeutralBothSpeedAndDirectionForStop() throws SerialPortException {
        arduinoSerialCommandAdapter.stop();

        final ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(serialPort, times(3)).writeString(argumentCaptor.capture());

        assertTrue("Did not receive the initialization command", argumentCaptor.getAllValues().contains("INI10\n"));
        assertTrue("Did not receive the command to set the speed to 0/neutral", argumentCaptor.getAllValues().contains("THR1000\n"));
        assertTrue("Did not receive the command to set the direction to 0/neutral", argumentCaptor.getAllValues().contains("STE110\n"));
    }
}
