package com.example.demo;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Task 3: Aggregator
 * Waits for all 3 correlated pieces (form, clearance, payment) sharing the same
 * permitId, then combines them into one Permit Application Summary.
 */
@Component
public class Task3AggregatorRoute extends RouteBuilder {

    @Autowired
    private LogStore logStore;

    @Override
    public void configure() throws Exception {
        from("seda:permitAggregate")
            .routeId("task3-aggregator")
            .aggregate(header("permitId"), new GroupedBodyAggregationStrategy())
            .completionSize(3)
            .completionTimeout(5000)
            .log("PERMIT SUMMARY [${header.permitId}]: ${body}")
            .process(exchange -> logStore.add("Task 3: summary for [" + exchange.getIn().getHeader("permitId") + "] " + exchange.getIn().getBody(String.class)))
            .to("seda:permitToTranslate");
    }
}
