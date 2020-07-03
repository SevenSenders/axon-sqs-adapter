package com.sevensenders.axon.jms;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Defines the properties to use for forwarding Event Messages published on the Event Bus to an JMS Message Broker.
 */
@ConfigurationProperties(prefix = "axon.jms")
public class JMSProperties {

    /**
     *  Queue name to publish event message
     */
    private String eventQueue;

    public String getEventQueue() {
        return eventQueue;
    }

    public void setEventQueue(String eventQueue) {
        this.eventQueue = eventQueue;
    }
}
