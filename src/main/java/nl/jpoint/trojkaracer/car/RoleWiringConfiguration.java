package nl.jpoint.trojkaracer.car;

import nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionHelper;
import nl.jpoint.trojkaracer.car.domain.driver.DriveCommand;
import nl.jpoint.trojkaracer.car.domain.driver.Driver;
import nl.jpoint.trojkaracer.car.domain.driver.SimpleDriver;
import nl.jpoint.trojkaracer.car.domain.driver.WrappingDriver;
import nl.jpoint.trojkaracer.car.domain.finisher.AutomaticallyTimedFinisher;
import nl.jpoint.trojkaracer.car.domain.finisher.Finisher;
import nl.jpoint.trojkaracer.car.domain.navigator.BasicNavigator;
import nl.jpoint.trojkaracer.car.domain.navigator.Navigator;
import nl.jpoint.trojkaracer.car.domain.starter.AutomaticallyTimedStarter;
import nl.jpoint.trojkaracer.car.domain.starter.Starter;
import org.opencv.core.Mat;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * Spring configuration class that programmatically wires the correct implementations to each role (driver, navigator etc.).
 */
@Configuration
public class RoleWiringConfiguration {

    @Bean
    public Navigator navigator(final Flux<Mat> viewPublisher, final ComputerVisionHelper computerVisionHelper) {
//        return new FixedPathNavigator();
        return new BasicNavigator(viewPublisher, computerVisionHelper);
    }

    @Bean
    public Driver manualDriver(final Publisher<DriveCommand> manualDriveCommandPublisher) {
        return new WrappingDriver(manualDriveCommandPublisher);
    }

    @Bean
    public Driver automatedDriver(final Navigator navigator) {
        return new SimpleDriver(navigator);
    }

    @Bean
    public Starter starter(final Flux<Mat> viewPublisher, final ComputerVisionHelper computerVisionHelper) {
        return new AutomaticallyTimedStarter();
//        return new RedLightStarter(viewPublisher, computerVisionHelper);
    }

    @Bean
    public Finisher finisher(final Flux<Mat> viewPublisher, final ComputerVisionHelper computerVisionHelper) {
//        return new NoFinisher();
        return new AutomaticallyTimedFinisher(Duration.ofSeconds(2));
    }
}
