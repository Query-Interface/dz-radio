package com.queryinterface.radio.ytdl.kafka.handlers;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.queryinterface.radio.ytdl.kafka.KafkaConfig;
import com.queryinterface.radio.ytdl.kafka.Topics;
import com.queryinterface.radio.ytdl.kafka.messages.TrackDownloadCommand;
import com.queryinterface.radio.ytdl.services.KafkaPublisherService;
import com.queryinterface.radio.ytdl.services.YtDownloadService;

@Component
@KafkaListener(topics = {"dz.playlist.commands"}, groupId = "ytdl", containerFactory = "kafkaListenerContainerFactory")
public class KafkaMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageHandler.class);
    private Map<HandlerInfo, Consumer<Message>> handlers = new HashMap<>();
    private YtDownloadService downloadService;
    private KafkaPublisherService publisherService;

    public KafkaMessageHandler(YtDownloadService service, KafkaPublisherService publisherService) {
        this.downloadService = service;
        this.publisherService = publisherService;
        registerHandlers();
    }

    @KafkaHandler(isDefault = true)
    public void handleKafkaMessage(@Payload Object message, @Headers MessageHeaders headers) {
        String messageType = getHeaderValueAsString(KafkaConfig.HEADER_TYPE, headers);
        String topic = getHeaderValueAsString(KafkaHeaders.RECEIVED_TOPIC, headers);
        logger.info("Received message {} on topic {}.", messageType, topic);
        Consumer<Message> handler = handlers.get(new HandlerInfo(topic, messageType));
        if (handler != null) {
            handler.accept(new Message(headers, message));
        }
    }

    private void registerHandlers() {
        handlers.put(new HandlerInfo(Topics.DZ_PLAYLIST_COMMANDS, "track.yt.download.command.v1"), this::handleDownloadRequest);
        //handlers.put(new HandlerInfo("dz.playlist.responses", "track.search.failed.v1"), this::handleDownloadFailed);
        //handlers.put(new HandlerInfo("dz.playlist.responses", "track.search.succeeded.v1"), this::handleDownloadSucceeded);
    }

    private void handleDownloadRequest(Message message) {
        var command = (TrackDownloadCommand) message.body;
        downloadService.download(command);
    }

    private String getHeaderValueAsString(String headerName, MessageHeaders headers) {
        var value = headers.get(headerName);
        if (value == null) {
            return "";
        }
        if (value instanceof byte[]) {
            return new String((byte[]) value, StandardCharsets.UTF_8);
        }
        return value.toString();
    }

    public static record HandlerInfo(String topic, String messageType) {

    }

    public static record Message(MessageHeaders headers, Object body) {

    }
}
