package com.sevensenders.axon.jms.message;

import java.util.Map;

public class JMSMessage {

    private byte[] payload;
    private Map<String, Object> metadata;

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
