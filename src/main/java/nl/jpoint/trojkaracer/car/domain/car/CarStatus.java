package nl.jpoint.trojkaracer.car.domain.car;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonView(Views.Public.class)
public class CarStatus {

    private final Speed speed;
    private final Direction direction;

}
