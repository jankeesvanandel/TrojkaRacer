package nl.jpoint.trojkaracer.car;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import nl.jpoint.trojkaracer.car.api.WebSocketCarControllerHandler;
import nl.jpoint.trojkaracer.car.api.WebSocketRaceControllerHandler;
import nl.jpoint.trojkaracer.car.application.RaceControlService;
import nl.jpoint.trojkaracer.car.domain.ViewRetriever;
import nl.jpoint.trojkaracer.car.domain.car.Car;
import nl.jpoint.trojkaracer.car.domain.car.CarEngine;
import nl.jpoint.trojkaracer.car.domain.car.CarStatus;
import nl.jpoint.trojkaracer.car.domain.computervision.ComputerVisionHelper;
import nl.jpoint.trojkaracer.car.domain.driver.DriveCommand;
import nl.jpoint.trojkaracer.car.domain.race.RaceStatus;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

/**
 * Main executable for the Car application. This class starts the whole Car Application.
 */
@SpringBootApplication(scanBasePackages = { "nl.jpoint.trojkaracer.car.infrastructure", "nl.jpoint.trojkaracer.car.application", "nl.jpoint.trojkaracer.car" })
public class CarApplication {

    private static final int IMAGE_FEED_INTERVAL_IN_MILLIS = 200;
    private static final int CAR_STATUS_REPLAY_DURATION_IN_MILLIS = 5000;
    private static final String CAR_WEBSOCKET_API_URL = "/websocket/car";
    private static final String RACE_WEBSOCKET_API_URL = "/websocket/race";

    /**
     * Application's main method that starts the Spring boot application with the context as defined in this class.
     * @param args the arguments provided to start te application.
     */
    public static void main(final String[] args) {
//        System.loadLibrary("opencv_java310");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        SpringApplication.run(CarApplication.class, args);
    }

    // Car Setup
    @Bean(destroyMethod = "shutdown")
    @Profile("production")
    public GpioController gpioController() {
        return GpioFactory.getInstance();
    }

    @Bean
    public Car car(final CarEngine carEngine) {
        return new Car(carEngine);
    }

    // Publishers and Subscribers Setup
    @Bean({ "carStatusSubscriber", "carStatusProcessor" })
    public UnicastProcessor<CarStatus> carStatusProcessor() {
        return UnicastProcessor.create();
    }

    @Bean({ "raceStatusSubscriber", "raceStatusPublisher" })
    public UnicastProcessor<RaceStatus> raceStatusProcessor() {
        return UnicastProcessor.create();
    }

    @Bean
    public Flux<CarStatus> carStatusFlux(final UnicastProcessor<CarStatus> carStatusProcessor) {
        return carStatusProcessor
                .replay(Duration.ofMillis(CAR_STATUS_REPLAY_DURATION_IN_MILLIS))
                .autoConnect();
    }

    @Bean
    public Flux<RaceStatus> raceStatusFlux(final UnicastProcessor<RaceStatus> raceStatusProcessor) {
        return raceStatusProcessor
                .replay(1)
                .autoConnect();
    }

    @Bean({ "manualDriveCommandSubscriber", "manualDriveCommandPublisher" })
    public DirectProcessor<DriveCommand> manualDriveCommandProcessor() {
        return DirectProcessor.create();
    }

    // WebSocket API Setup
    @Bean
    public HandlerMapping handlerMapping(final Flux<CarStatus> carStatusFlux,
                                         final Flux<RaceStatus> raceStatusFlux,
                                         final ObjectMapper objectMapper,
                                         final CoreSubscriber<DriveCommand> manualDriveCommandSubscriber,
                                         final RaceControlService raceControlService) {
        final Map<String, WebSocketHandler> map = new HashMap<>();
        map.put(CAR_WEBSOCKET_API_URL, new WebSocketCarControllerHandler(carStatusFlux, objectMapper, manualDriveCommandSubscriber));
        map.put(RACE_WEBSOCKET_API_URL, new WebSocketRaceControllerHandler(raceStatusFlux, objectMapper, raceControlService));

        final SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(10);
        mapping.setUrlMap(map);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter(webSocketService());
    }

    @Bean
    public WebSocketService webSocketService() {
        return new HandshakeWebSocketService(new ReactorNettyRequestUpgradeStrategy());
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


    // Image publishing Flux setup
    @Bean
    public Flux<Mat> viewPublisher(final ViewRetriever viewRetriever) {
        return Flux.interval(Duration.ofMillis(IMAGE_FEED_INTERVAL_IN_MILLIS))
                .map(index -> viewRetriever.getView())
                .map(frame -> {
                    Core.flip(frame, frame, -1);
                    return frame;
                });
    }

    @Bean(destroyMethod = "release")
    public VideoCapture camera() {
        return new VideoCapture(0);
    }

    @Bean
    public ComputerVisionHelper computerVisionHelper(@Value("debug.image.storage.path") final String imagePath) {
        return new ComputerVisionHelper("/home/pi/");
    }

}
