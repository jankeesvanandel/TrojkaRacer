package nl.jpoint.trojkaracer.runtime;

import nl.jpoint.trojkaracer.processing.ImageProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.ObjectGraph;

/**
 * Main Trojka Racer application that initializes all components, and triggers/starts all loops.
 */
public class TrojkaRacerRunner {

    private static final int MILLIS = 1000;
    private static final int NR_OF_SCANS_PER_SECOND = 10;
    private static final int NR_OF_MAIN_LOOPS_PER_SECOND = 10;
    private static final long EXECUTOR_DELAY_IN_MILLIS = 100L;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrojkaRacerRunner.class);

    private final TrojkaRacerMainLoop trojkaRacerMainLoop;
    private final ImageProcessor imageProcessor;
    private final ScheduledExecutorService scheduledExecutorService;


    @Inject
    TrojkaRacerRunner(final TrojkaRacerMainLoop trojkaRacerMainLoop,
                      final ImageProcessor imageProcessor,
                      final ScheduledExecutorService scheduledExecutorService) {
        this.trojkaRacerMainLoop = trojkaRacerMainLoop;
        this.imageProcessor = imageProcessor;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    /**
     * Main method that starts all services/components.
     * @param args the command line arguments.
     */
    public static void main(final String[] args) {
        final ObjectGraph objectGraph = ObjectGraph.create(new TrojkaRacerModule());
        final TrojkaRacerRunner trojkaRacerRunner = objectGraph.get(TrojkaRacerRunner.class);
        trojkaRacerRunner.start();
    }

    /**
     * Start method that starts the actual service by scheduling the different loops.
     */
    void start() {
        try {
            LOGGER.info("Starting the Trojka Racer Runner; setting up the different service loops");
            final ScheduledFuture scheduledFutureImageProcessor = scheduledExecutorService.scheduleAtFixedRate(
                    imageProcessor, EXECUTOR_DELAY_IN_MILLIS, (long) MILLIS / NR_OF_SCANS_PER_SECOND, TimeUnit.MILLISECONDS);
            final ScheduledFuture scheduledFutureMainLoop = scheduledExecutorService.scheduleAtFixedRate(
                    trojkaRacerMainLoop, EXECUTOR_DELAY_IN_MILLIS, (long) MILLIS / NR_OF_MAIN_LOOPS_PER_SECOND, TimeUnit.MILLISECONDS);

            while (!scheduledFutureImageProcessor.isCancelled() && !scheduledFutureMainLoop.isCancelled())  {
                Thread.sleep(10);
                // Do nothing
            }
        } catch (Exception e) {
            LOGGER.error("System stopped due to exception in one of the threads/loops: ", e);
        } finally {
            LOGGER.warn("Exiting application/service");
        }
    }

}
