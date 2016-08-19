package nl.jpoint.trojkaracer.web.ai;


import nl.jpoint.trojkaracer.web.ai.controller.PropertyController;
import nl.jpoint.trojkaracer.web.ai.transformers.JsonTransformer;

import javax.inject.Inject;

import static spark.Spark.get;
import static spark.Spark.port;

public class ConfigurationWebApp {

    @Inject
    PropertyController propertyController;

    public void run(){

        port(9999);
        get("/properties","application/json", (request, response) -> propertyController.getAll(), new JsonTransformer());
    }


}
