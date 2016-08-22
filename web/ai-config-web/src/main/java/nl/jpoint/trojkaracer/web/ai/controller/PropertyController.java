package nl.jpoint.trojkaracer.web.ai.controller;

import javax.inject.Inject;
import java.util.Properties;

public class PropertyController {

    @Inject
    PropertiesStorage propertiesStorage;

    public Properties getAll(){

        return propertiesStorage.getAll();
    }
}
