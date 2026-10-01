package com.example.demo;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Task 1: Message Channel
 * Receives the raw application from the web form (via seda:permitProcessingSystem)
 * and forwards it into the routing stage (Task 2). Also tags the exchange with a
 * forceFailure header if the Owner field was set to "FAIL" - this lets Task 5
 * simulate a failure on demand, since the original body text doesn't survive
 * past Task 4's translation step.
 */
@Component
public class Task1MessageChannelRoute extends RouteBuilder {

    @Autowired
    private LogStore logStore;

    @Override
    public void configure() throws Exception {
        from("seda:permitProcessingSystem")
            .routeId("task1-target")
            .process(exchange -> {
                exchange.getIn().setHeader("permitId", "BP-" + System.currentTimeMillis());
                String body = exchange.getIn().getBody(String.class);
                boolean forceFailure = body != null && body.contains("Owner: FAIL");
                exchange.getIn().setHeader("forceFailure", forceFailure);
            })
            .log("BUSINESS PERMIT OFFICE received [${header.permitId}]: ${body}")
            .process(exchange -> logStore.add("Task 1: received [" + exchange.getIn().getHeader("permitId") + "] " + exchange.getIn().getBody(String.class)))
            .to("seda:permitRouter");
    }
} 
