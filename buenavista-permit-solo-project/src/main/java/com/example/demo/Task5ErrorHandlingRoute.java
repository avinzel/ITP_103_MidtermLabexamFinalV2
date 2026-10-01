package com.example.demo;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Task 5: Error Handling
 * Final stage of the pipeline. Simulates system downtime either on demand
 * (submit an application with Owner = "FAIL", tagged as a header back in Task 1)
 * or randomly (~30% of other runs). Retries up to 3 times, then routes to
 * manual review if still failing. Otherwise the permit is approved.
 */
@Component
public class Task5ErrorHandlingRoute extends RouteBuilder {

    @Autowired
    private LogStore logStore;

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
            .maximumRedeliveries(3)
            .redeliveryDelay(1000)
            .handled(true)
            .log("Retries exhausted, sending to manual review: ${body}")
            .process(exchange -> logStore.add("Task 5: FLAGGED for manual review - " + exchange.getIn().getBody(String.class)))
            .to("seda:manualReviewQueue");

        from("seda:finalizePermit")
            .routeId("task5-finalize")
            .process(exchange -> {
                Boolean forcedFailure = exchange.getIn().getHeader("forceFailure", Boolean.class);
                boolean shouldFail = (forcedFailure != null && forcedFailure) || Math.random() < 0.3;
                if (shouldFail) {
                    throw new RuntimeException("Simulated permit system downtime");
                }
            })
            .log("PERMIT APPROVED: ${body}")
            .process(exchange -> logStore.add("Task 5: PERMIT APPROVED - " + exchange.getIn().getBody(String.class)));

        from("seda:manualReviewQueue")
            .routeId("task5-manualReview")
            .log("Manual review received: ${body}");
    }
}