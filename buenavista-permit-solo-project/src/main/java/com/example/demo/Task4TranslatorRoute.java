package com.example.demo;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.StringReader;
import org.xml.sax.InputSource;

/**
 * Task 4: Message Translator
 * Converts the aggregated permit summary from an XML representation
 * (simulating the old LGU system) into JSON (for the new online portal).
 * Uses plain javax.xml.parsers - no extra Camel dataformat dependency needed.
 */
@Component
public class Task4TranslatorRoute extends RouteBuilder {

    @Autowired
    private LogStore logStore;

    @Override
    public void configure() throws Exception {
        from("seda:permitToTranslate")
            .routeId("task4-translator")
            .process(exchange -> {
                String permitId = exchange.getIn().getHeader("permitId", String.class);
                String xml = "<BusinessPermitApplication><PermitId>" + permitId
                        + "</PermitId><Status>Aggregated</Status></BusinessPermitApplication>";
                exchange.getIn().setBody(xml);
            })
            .log("OLD LGU SYSTEM (XML): ${body}")
            .process(exchange -> {
                String xml = exchange.getIn().getBody(String.class);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(xml)));
                Element root = doc.getDocumentElement();
                NodeList children = root.getChildNodes();
                StringBuilder json = new StringBuilder("{");
                for (int i = 0; i < children.getLength(); i++) {
                    if (children.item(i) instanceof Element field) {
                        json.append("\"").append(field.getTagName()).append("\":\"")
                            .append(field.getTextContent()).append("\"");
                        if (i < children.getLength() - 1) json.append(",");
                    }
                }
                json.append("}");
                exchange.getIn().setBody(json.toString());
            })
            .log("NEW ONLINE PORTAL (JSON): ${body}")
            .process(exchange -> logStore.add("Task 4: " + exchange.getIn().getBody(String.class)))
            .to("seda:finalizePermit");
    }
}
