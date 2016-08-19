package nl.jpoint.trojkaracer.web.ai;

import dagger.ObjectGraph;
import nl.jpoint.trojkaracer.web.ai.controller.PropertyController;
import nl.jpoint.trojkaracer.web.ai.modules.AiWebappModule;

import javax.inject.Inject;

public class Runner {
    @Inject
    PropertyController propertieController;

    @Inject
    static Runner runner;

    public static void main(String[] args) {
        ObjectGraph objectGraph = ObjectGraph.create(new AiWebappModule());
        ConfigurationWebApp webApp = objectGraph.get(ConfigurationWebApp.class);

        webApp.run();

    }
}
