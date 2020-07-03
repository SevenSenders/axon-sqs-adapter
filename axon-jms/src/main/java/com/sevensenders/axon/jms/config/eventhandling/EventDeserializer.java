package com.sevensenders.axon.jms.config.eventhandling;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EventDeserializer extends StdDeserializer<GenericDomainEventMessage> {

    protected EventDeserializer(Class vc) {
        super(vc);
    }

    public EventDeserializer() {
        this(null);
    }

    @Override
    public GenericDomainEventMessage deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        JsonNode node = parser.getCodec().readTree(parser);
        String type = node.get("type").asText();
        String aggId = node.get("aggregateIdentifier").asText();
        int sequenceNumber = node.get("sequenceNumber").asInt();
        String identifier = node.get("identifier").asText();
        String timestamp = node.get("timestamp").asText();
        String payloadType = node.get("payloadType").asText();

      /*  Iterator<Map.Entry<String, JsonNode>> it = node.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            String name = entry.getKey();
        }*/
        String traceId = node.get("metaData").get("traceId").asText();
        String correlationId = node.get("metaData").get("correlationId").asText();
        Map<String,String> header = new HashMap<>();
        header.put("traceId" , traceId);
        header.put("correlationId", correlationId);

        node.get("payload").asText();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);

        try {
            JsonNode payloadNode = node.get("payload");
            Class<?> eventClass = Class.forName(payloadType);
            Object obj = newInstance(eventClass);

            Iterator<Map.Entry<String, JsonNode>> it = payloadNode.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                String name = entry.getKey();
                Field field = ReflectionUtils.findField(eventClass,name);field.setAccessible(true);
                field.set(obj,entry.getValue().asText());
            }



            //mapper.readValue(node.get("payload").asText(),Class.forName(payloadType));
            GenericDomainEventMessage message = new GenericDomainEventMessage<>(type,aggId,sequenceNumber, obj,
                                           header,identifier, Instant.now());
            return message;
        } catch (ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }


        return null;
    }

    public static <T> T newInstance(Class<T> klass, Object... args) {
        try {
            Constructor<T> ctor;

            if (args.length == 0) {
                ctor = klass.getDeclaredConstructor();
            } else {
                Class<?>[] argClses = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    argClses[i] = args[i].getClass();
                }
                ctor = klass.getDeclaredConstructor(argClses);
            }
            ctor.setAccessible(true);
            return ctor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
