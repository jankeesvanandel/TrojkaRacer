package nl.jpoint.trojkaracer.car.domain;

import org.opencv.core.Mat;

/**
 * Interface for retrieving a single View.
 */
public interface ViewRetriever {

    /**
     * Retrieves a view and returns that view.
     * @return a view and returns that view.
     */
    Mat getView();

}
