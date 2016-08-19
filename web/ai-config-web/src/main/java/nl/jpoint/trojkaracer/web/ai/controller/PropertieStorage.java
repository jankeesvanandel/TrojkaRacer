package nl.jpoint.trojkaracer.web.ai.controller;

import nl.jpoint.trojkaracer.web.config.ApplicationProperties;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Properties;

public class PropertieStorage {

    @Inject
    ApplicationProperties applicationProperties;

    private final Properties properties;

    @Inject
    public PropertieStorage(final ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        properties = new Properties();
        try {
            properties.load(this.getClass().getResourceAsStream(applicationProperties.getProperty("storage.file")));
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public Properties getAll(){
        return properties;
    }

}
