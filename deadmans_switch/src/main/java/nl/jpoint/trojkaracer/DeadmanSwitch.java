package nl.jpoint.trojkaracer;

import nl.jpoint.trojkaracer.pid.Killable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Thread that tracks connection with host. If connection is lost, the killable will be killed.
 */
public class DeadmanSwitch implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeadmanSwitch.class);

    // interval in millis
    private static final int INTERVAL = 1000;
    // Timeout in millis
    private static final int TIMEOUT = 200;

    private final Killable killable;
    private final InetAddress hostToVerifyConnection;

    /**
     * Deadman Switch that stops the Trojka when (WiFi) connection is lost.
     * @param killable The process that needs to be stopped.
     * @param hostToVerifyConnection The host that is pinged to validate the (WiFi)connection.
     */
    @Inject
    public DeadmanSwitch(Killable killable, InetAddress hostToVerifyConnection) {
        this.killable = killable;
        this.hostToVerifyConnection = hostToVerifyConnection;
    }

    @Override
    public void run() {
        try {
            while (isConnected()) {
                Thread.sleep(INTERVAL);
            }
            // No longer connected
            LOGGER.warn("Lost connection....killing all processes");
            killable.kill();
        } catch (Exception e) {
            LOGGER.warn("Exception caught in DeadmanSwitch, therefore killing all processes", e);
            killable.kill();
        }
    }

    private boolean isConnected() throws IOException {
        return hostToVerifyConnection.isReachable(TIMEOUT);
    }
}
