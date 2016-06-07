package nl.jpoint.trojkaracer.pid;

/**
 * This class represents a "killable" object, or at least an object that runs a loop. It can be subclassed to represent an object/loop that the should be
 * able to be killed/stopped from outside.
 */
public interface Killable {

    void kill();

}
