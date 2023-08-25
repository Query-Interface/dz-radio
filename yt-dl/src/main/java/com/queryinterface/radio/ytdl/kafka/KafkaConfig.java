package com.queryinterface.radio.ytdl.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.queryinterface.radio.ytdl.kafka.messages.TrackDownloadFailed;
import com.queryinterface.radio.ytdl.kafka.messages.TrackDownloadSucceeded;
import com.queryinterface.radio.ytdl.kafka.messages.TrackDownloadCommand;


@EnableKafka
@Configuration
public class KafkaConfig {

    public static final String HEADER_TYPE = "type";
    public static final String HEADER_TOPIC = "topic";
    @Value("${app.kafka.url:kafka:9092}")
    private String kafkaUrl;

    @Bean
    public ConsumerFactory<String, String> consumerFactory(Deserializer<String> kafkaDeserializer) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "radio");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
                kafkaDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String>
                factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(getDeserializer()));
        return factory;
    }

    static Map<String, Class<?>> getClassMapping() {
        Map<String, Class<?>> mapping = new HashMap<>();
        mapping.put("track.yt.download.command.v1", TrackDownloadCommand.class);
        mapping.put("track.yt.download.succeeded.v1", TrackDownloadSucceeded.class);
        mapping.put("track.yt.download.failed.v1", TrackDownloadFailed.class);
        // ...
        return mapping;
    }

    public static JavaType getMessageType(byte[] data, org.apache.kafka.common.header.Headers headers) {
        Header header = headers.lastHeader(KafkaConfig.HEADER_TYPE);
        var type = new String(header.value());
        var clazz = getClassMapping().get(type);
        return TypeFactory.defaultInstance().constructType(clazz);
    }

    @Bean(name="kafkaDeserializer")
    public Deserializer<String> getDeserializer() {
        Deserializer<String> deserializer;
        Map<String, Class<?>> mapping = getClassMapping();
        try (org.springframework.kafka.support.serializer.JsonDeserializer<String> jsonDeserializer = getJsonDeserializer(mapping)) {
            deserializer = new KafkaDeserializer<>(mapping, jsonDeserializer);
        }
        return deserializer;
    }

    public JsonDeserializer<String> getJsonDeserializer(Map<String, Class<?>> classMapping) {
        ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new Jdk8Module())
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();
        JsonDeserializer<String> jsonDeserializer = new JsonDeserializer<>(objectMapper);
        Map<String, Object> configs = new HashMap<>();
        configs.put(JsonDeserializer.TYPE_MAPPINGS, buildMappingString());
        configs.put(JsonDeserializer.REMOVE_TYPE_INFO_HEADERS, false);
        configs.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);
        configs.put(JsonDeserializer.VALUE_TYPE_METHOD, "com.queryinterface.radio.ytdl.kafka.KafkaConfig.getMessageType");
        jsonDeserializer.configure(configs, false);

        return jsonDeserializer;
    }

    private String buildMappingString() {
        Map<String, Class<?>> mappings = getClassMapping();
        StringBuilder mapping = new StringBuilder();
        var entries = mappings.entrySet();
        for (Entry<String,Class<?>> entry : entries) {
            mapping.append(entry.getKey()).append(':').append(entry.getValue().getName());
            mapping.append(',');
        }
        var result = mapping.toString();
        return result.substring(0, result.length() - 1);
    }
}
