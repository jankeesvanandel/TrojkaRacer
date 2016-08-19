package nl.jpoint.trojkaracer.web.ai.controller;

import javax.inject.Inject;
import java.util.Properties;

public class PropertyController {

    @Inject
    PropertieStorage propertieStorage;

    public Properties getAll(){

        return propertieStorage.getAll();
    }
}
