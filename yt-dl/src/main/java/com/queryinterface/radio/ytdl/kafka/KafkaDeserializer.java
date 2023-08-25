package com.queryinterface.radio.ytdl.kafka;

import java.util.Map;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

public class KafkaDeserializer<T> implements Deserializer<T> {
    
    private final Map<String, Class<?>> classMapping;
    private final JsonDeserializer<T> jsonDeserializer;

    public KafkaDeserializer(
            Map<String, Class<?>> classMapping,
            JsonDeserializer<T> jsonDeserializer) {
        this.classMapping = classMapping;
        this.jsonDeserializer = jsonDeserializer;
        //((DefaultJackson2JavaTypeMapper) this.jsonDeserializer.getTypeMapper()).setClassIdFieldName(KafkaConfig.HEADER_TYPE);
    }

    @Override
    public T deserialize(String topic, Headers headers, byte[] data) {
        String type = getHeaderAsString(KafkaConfig.HEADER_TYPE, headers);
        Class<?> messageType = this.classMapping.get(type);
        if (messageType != null) {
            return jsonDeserializer.deserialize(topic, headers, data);
        }
        return null;
    }
    
    private String getHeaderAsString(String headerName, Headers headers) {
        Header header = headers.lastHeader(headerName);
        return new String(header.value());
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        jsonDeserializer.configure(configs, isKey);
    }
    
    @Override
    public T deserialize(String topic, byte[] data) {
        throw new RuntimeException("Not implemented");
    }
}
