package com.example.demo;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Task 2: Content-Based Router
 * Routes the application to the Local Business Permit Office (if within Buenavista)
 * or Provincial/External Processing (otherwise), then fans out into the 3 pieces
 * Task 3's aggregator needs: application form, barangay clearance, payment confirmation.
 */
@Component
public class Task2ContentRouterRoute extends RouteBuilder {

    @Autowired
    private LogStore logStore;

    @Override
    public void configure() throws Exception {

        from("seda:permitRouter")
            .routeId("task2-router")
            .choice()
                .when(simple("${body} contains 'Location: Buenavista'"))
                    .log("Routing to LOCAL office: ${body}")
                    .process(exchange -> logStore.add("Task 2 (Local): [" + exchange.getIn().getHeader("permitId") + "]"))
                .otherwise()
                    .log("Routing to PROVINCIAL/EXTERNAL: ${body}")
                    .process(exchange -> logStore.add("Task 2 (External): [" + exchange.getIn().getHeader("permitId") + "]"))
            .end()
            .multicast()
                .to("direct:formPart", "direct:clearancePart", "direct:paymentPart")
            .end();

        from("direct:formPart")
            .routeId("task2-form")
            .setBody(simple("ApplicationForm[permitId=${header.permitId}]"))
            .to("seda:permitAggregate");

        from("direct:clearancePart")
            .routeId("task2-clearance")
            .setBody(simple("BarangayClearance[permitId=${header.permitId}, status=CLEARED]"))
            .to("seda:permitAggregate");

        from("direct:paymentPart")
            .routeId("task2-payment")
            .setBody(simple("PaymentConfirmation[permitId=${header.permitId}, status=PAID]"))
            .to("seda:permitAggregate");
    }
}
