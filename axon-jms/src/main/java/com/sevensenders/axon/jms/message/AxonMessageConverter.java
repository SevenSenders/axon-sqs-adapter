package com.sevensenders.axon.jms.message;

import org.axonframework.common.DateTimeUtils;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.messaging.Headers;
import org.axonframework.messaging.MetaData;
import org.axonframework.serialization.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.axonframework.common.DateTimeUtils.formatInstant;
import static org.axonframework.messaging.Headers.MESSAGE_TIMESTAMP;

public class AxonMessageConverter {

    private final Serializer serializer;

    public AxonMessageConverter(Serializer serializer) {
        this.serializer = serializer;
    }

    public JMSMessage createMessage(EventMessage<?> eventMessage) {
        final JMSMessage jmsMessage = new JMSMessage();
        SerializedObject<byte[]> serializedObject = eventMessage.serializePayload(serializer, byte[].class);
        jmsMessage.setPayload(serializedObject.getData());

        Map<String, Object> headers = new HashMap<>();
        eventMessage.getMetaData().forEach((k, v) -> headers.put(Headers.MESSAGE_METADATA + "-" + k, v));
        Headers.defaultHeaders(eventMessage, serializedObject).forEach((k, v) -> {
            if (k.equals(MESSAGE_TIMESTAMP)) {
                headers.put(k, formatInstant(eventMessage.getTimestamp()));
            } else {
                headers.put(k, v);
            }
        });
        jmsMessage.setMetadata(headers);
        return jmsMessage;
    }

    public Optional<EventMessage<?>> readMessage(JMSMessage message) {
        Map<String, Object> metadata = message.getMetadata();
        if (!metadata.keySet().containsAll(Arrays.asList(Headers.MESSAGE_ID, Headers.MESSAGE_TYPE))) {
            return Optional.empty();
        }

        Map<String, Object> properties = new HashMap<>();
        metadata.forEach((k, v) -> {
            if (k.startsWith(Headers.MESSAGE_METADATA + "-")) {
                properties.put(k.substring((Headers.MESSAGE_METADATA + "-").length()), v);
            }
        });

        SimpleSerializedObject<byte[]> serializedMessage = new SimpleSerializedObject<>(
                message.getPayload(), byte[].class,
                Objects.toString(metadata.get(Headers.MESSAGE_TYPE)),
                Objects.toString(metadata.get(Headers.MESSAGE_REVISION), null)
        );
        SerializedMessage<?> eventMessage = new SerializedMessage<>(
                Objects.toString(metadata.get(Headers.MESSAGE_ID)),
                new LazyDeserializingObject<>(serializedMessage, serializer),
                new LazyDeserializingObject<>(MetaData.from(properties))
        );
        String timestamp = Objects.toString(metadata.get(MESSAGE_TIMESTAMP));
        if (metadata.containsKey(Headers.AGGREGATE_ID)) {
            return Optional.of(new GenericDomainEventMessage<>(Objects.toString(metadata.get(Headers.AGGREGATE_TYPE)),
                    Objects.toString(metadata.get(Headers.AGGREGATE_ID)),
                    (Integer) metadata.get(Headers.AGGREGATE_SEQ),
                    eventMessage, () -> DateTimeUtils.parseInstant(timestamp)));
        } else {
            return Optional.of(new GenericEventMessage<>(eventMessage, () -> DateTimeUtils.parseInstant(timestamp)));
        }
    }
}
