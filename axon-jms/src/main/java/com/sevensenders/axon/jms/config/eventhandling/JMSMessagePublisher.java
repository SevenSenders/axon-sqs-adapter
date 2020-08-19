package com.sevensenders.axon.jms.config.eventhandling;

import com.sevensenders.axon.jms.message.AxonMessageConverter;
import org.axonframework.common.Registration;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.lifecycle.Phase;
import org.axonframework.lifecycle.ShutdownHandler;
import org.axonframework.lifecycle.StartHandler;
import org.axonframework.messaging.EventPublicationFailedException;
import org.axonframework.messaging.SubscribableMessageSource;
import org.springframework.jms.core.JmsTemplate;

import java.util.Arrays;
import java.util.List;

public class JMSMessagePublisher {

    private final SubscribableMessageSource<EventMessage<?>> messageSource;

    private final JmsTemplate jmsTemplate;

    private final AxonMessageConverter axonMessageConverter;

    private Registration eventBusRegistration;

    private String queueName;

    public JMSMessagePublisher(SubscribableMessageSource<EventMessage<?>> messageSource,
                               JmsTemplate jmsTemplate, AxonMessageConverter axonMessageConverter) {
        this.messageSource = messageSource;
        this.jmsTemplate = jmsTemplate;
        this.axonMessageConverter = axonMessageConverter;
    }

    @StartHandler(phase = Phase.INBOUND_EVENT_CONNECTORS)
    public void start() {
        eventBusRegistration = messageSource.subscribe(this::send);
    }

    @ShutdownHandler(phase = Phase.INBOUND_EVENT_CONNECTORS)
    public void shutDown() {
        if (eventBusRegistration != null) {
            eventBusRegistration.cancel();
            eventBusRegistration = null;
        }
    }

    protected void send(List<? extends EventMessage<?>> events) {
        for (EventMessage event : events) {
            try {
                Arrays.stream(queueName.split(","))
                        .forEach(queue -> {
                            jmsTemplate.convertAndSend(queue, axonMessageConverter.createMessage(event));});
            } catch (Exception e) {
                throw new EventPublicationFailedException("Error while sending message", e);
            }
        }
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
}
