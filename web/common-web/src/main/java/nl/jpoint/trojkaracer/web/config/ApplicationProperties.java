package nl.jpoint.trojkaracer.web.config;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Properties;

public class ApplicationProperties extends Properties{

    private static final String PROPERTY_FILE_LOCATION = "/application.properties";

    @Inject
    public ApplicationProperties(){
        super();
        try {
            this.load(this.getClass().getResourceAsStream(PROPERTY_FILE_LOCATION));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
