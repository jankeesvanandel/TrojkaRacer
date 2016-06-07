package nl.jpoint.trojkaracer;

import nl.jpoint.trojkaracer.pid.Killable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.InetAddress;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeadmanSwitchTest {

    @Mock
    private Killable killableMock;

    @Mock
    private InetAddress hostMock;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        when(hostMock.isReachable(anyInt())).thenReturn(true);
    }

    @Test
    public void testWithReachableHost() throws InterruptedException, IOException {
        DeadmanSwitch deadmanSwitch = new DeadmanSwitch(killableMock, hostMock);

        Thread thread = new Thread(deadmanSwitch);
        thread.start();

        thread.join(300);
        assertTrue(thread.isAlive());
        verify(killableMock, times(0)).kill();
    }

    @Test
    public void testKillableCalledWhenHostIsNotReachable() throws InterruptedException, IOException {
        when(hostMock.isReachable(anyInt())).thenReturn(false);
        DeadmanSwitch deadmanSwitch = new DeadmanSwitch(killableMock, hostMock);

        Thread thread = new Thread(deadmanSwitch);
        thread.start();

        thread.join(1000);
        verify(killableMock, times(1)).kill();
    }

}
