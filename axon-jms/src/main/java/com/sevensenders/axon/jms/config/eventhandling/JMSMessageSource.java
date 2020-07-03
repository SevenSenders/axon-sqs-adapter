package com.sevensenders.axon.jms.config.eventhandling;

import com.sevensenders.axon.jms.message.JMSMessage;
import com.sevensenders.axon.jms.message.AxonMessageConverter;
import org.axonframework.common.Registration;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.SubscribableMessageSource;
import org.springframework.messaging.Message;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class JMSMessageSource implements SubscribableMessageSource<EventMessage<?>> {

    private final AxonMessageConverter axonMessageConverter;

    private final List<Consumer<List<? extends EventMessage<?>>>> eventProcessors = new CopyOnWriteArrayList<>();

    public JMSMessageSource(AxonMessageConverter axonMessageConverter) {
        this.axonMessageConverter = axonMessageConverter;
    }

    @Override
    public Registration subscribe(Consumer<List<? extends EventMessage<?>>> messageProcessor) {
        eventProcessors.add(messageProcessor);
        return () -> eventProcessors.remove(messageProcessor);
    }

    public void receiveMessage(final Message message) {
        if (!eventProcessors.isEmpty()) {
            axonMessageConverter.readMessage((JMSMessage) message.getPayload())
                    .ifPresent(event -> eventProcessors.forEach(ep -> ep.accept(Collections.singletonList(event))));
        }
    }

}

