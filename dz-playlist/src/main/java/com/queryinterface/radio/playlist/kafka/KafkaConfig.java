package com.queryinterface.radio.playlist.kafka;

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
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper.TypePrecedence;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.queryinterface.radio.playlist.kafka.messages.PlaylistRefreshMessage;
import com.queryinterface.radio.playlist.kafka.messages.TrackDownloadFailed;
import com.queryinterface.radio.playlist.kafka.messages.TrackDownloadSucceeded;
import com.queryinterface.radio.playlist.kafka.messages.TrackDownloaded;
import com.queryinterface.radio.playlist.kafka.messages.TrackSearchFailed;
import com.queryinterface.radio.playlist.kafka.messages.TrackSearchSucceeded;
import com.queryinterface.radio.playlist.kafka.messages.TrackYtDownloadCommand;

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

    /*
    @Bean
    public ConsumerFactory<String, Playlist> dummyConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "radio");
        props.put(JsonObjectDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
                new JsonObjectDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Playlist> dummykafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Playlist>
                factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(dummyConsumerFactory());
        return factory;
    }
    */

    static Map<String, Class<?>> getClassMapping() {
        Map<String, Class<?>> mapping = new HashMap<>();
        mapping.put("track.downloaded.v1", TrackDownloaded.class);
        mapping.put("playlist.refresh.v1", PlaylistRefreshMessage.class);
        mapping.put("track.search.succeeded.v1", TrackSearchSucceeded.class);
        mapping.put("track.search.failed.v1", TrackSearchFailed.class);
        mapping.put("track.yt.download.command.v1", TrackYtDownloadCommand.class);
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

    private DefaultJackson2JavaTypeMapper buildKafkaClassMapper() {
        DefaultJackson2JavaTypeMapper classMapper = new DefaultJackson2JavaTypeMapper();
        classMapper.addTrustedPackages("*");
        classMapper.setTypePrecedence(TypePrecedence.TYPE_ID);
        classMapper.setClassIdFieldName(HEADER_TYPE);
        classMapper.setIdClassMapping(getClassMapping());

        return classMapper;
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
        configs.put(JsonDeserializer.VALUE_TYPE_METHOD, "com.queryinterface.radio.playlist.kafka.KafkaConfig.getMessageType");
        jsonDeserializer.configure(configs, false);

        //jsonDeserializer.setTypeMapper(buildKafkaClassMapper());
        //jsonDeserializer.setRemoveTypeHeaders(false);
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
