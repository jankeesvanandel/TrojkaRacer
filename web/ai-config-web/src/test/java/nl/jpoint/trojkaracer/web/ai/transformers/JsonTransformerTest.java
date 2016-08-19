package nl.jpoint.trojkaracer.web.ai.transformers;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class JsonTransformerTest {

    JsonTransformer transformer = new JsonTransformer();

    @org.junit.Test
    public void testRender() throws Exception {
        Properties props = new Properties();
        props.setProperty("key", "value");
        props.setProperty("key2", "1");

        String s = transformer.render(props);
        assertEquals("{\"key\":\"value\",\"key2\":\"1\"}",s);
    }
}