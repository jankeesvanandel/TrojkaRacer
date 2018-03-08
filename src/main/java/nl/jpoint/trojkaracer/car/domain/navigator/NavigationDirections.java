package nl.jpoint.trojkaracer.car.domain.navigator;

import lombok.Value;

/**
 * Class that provides navigational directions from the Navigator to the Driver.
 */
@Value(staticConstructor = "of")
public class NavigationDirections {

    private final int degrees;

}
