package nl.jpoint.trojkaracer.hardwareinterface;

/**
 * Interface that provides a method to stop a controller (or component) from working and that should set all controlled hardware to its default/neutral state.
 * It is up to the implementation if the controller (component) has to be reset before it can work again.
 */
@FunctionalInterface
public interface Stoppable {

    /**
     * Stops the controller/component from working and sets all controlled hardware to its default/neutral state.
     */
    void stop();

}
