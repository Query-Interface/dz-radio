package com.queryinterface.radio.playlist.kafka.handlers;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
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

import com.queryinterface.radio.playlist.dto.SearchResultDto;
import com.queryinterface.radio.playlist.kafka.KafkaConfig;
import com.queryinterface.radio.playlist.kafka.Topics;
import com.queryinterface.radio.playlist.kafka.messages.PlaylistRefreshMessage;
import com.queryinterface.radio.playlist.kafka.messages.TrackDownloadFailed;
import com.queryinterface.radio.playlist.kafka.messages.TrackDownloadSucceeded;
import com.queryinterface.radio.playlist.kafka.messages.TrackSearchCommand;
import com.queryinterface.radio.playlist.kafka.messages.TrackSearchSucceeded;
import com.queryinterface.radio.playlist.kafka.messages.TrackDownloaded;
import com.queryinterface.radio.playlist.kafka.messages.TrackYtDownloadCommand;
import com.queryinterface.radio.playlist.services.KafkaPublisherService;
import com.queryinterface.radio.playlist.services.PlaylistService;
import com.queryinterface.radio.playlist.PlaylistController;
import com.queryinterface.radio.playlist.Track;
import com.queryinterface.radio.playlist.TrackStatus;

@Component
@KafkaListener(topics = {"dz.playlist.commands","dz.playlist.responses","dz.playlist.events"}, groupId = "radio", containerFactory = "kafkaListenerContainerFactory")
// listen to several topics: https://stackoverflow.com/questions/56876702/how-to-subscribe-multiple-topic-using-kafkalistner-annotation
public class KafkaMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class);
    private Map<HandlerInfo, Consumer<Message>> handlers = new HashMap<>();
    private PlaylistService playlistService;
    private KafkaPublisherService publisherService;

    public KafkaMessageHandler(PlaylistService service, KafkaPublisherService publisherService) {
        this.playlistService = service;
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
        handlers.put(new HandlerInfo("dz.playlist.commands", "playlist.refresh.v1"), this::handleRefreshTracks);
        handlers.put(new HandlerInfo("dz.playlist.events", "track.downloaded.v1"), this::handleTrackDownloaded);
        handlers.put(new HandlerInfo("dz.playlist.responses", "track.search.failed.v1"), this::handleSearchFailed);
        handlers.put(new HandlerInfo("dz.playlist.responses", "track.search.succeeded.v1"), this::handleSearchSucceeded);
        handlers.put(new HandlerInfo("dz.playlist.responses", "track.yt.download.succeeded.v1"), this::handleYtDownloadSucceeded);
        handlers.put(new HandlerInfo("dz.playlist.responses", "track.yt.download.failed.v1"), this::handleYtDownloadFailed);
    }

    private void handleTrackDownloaded(Message message) {
        var track = (TrackDownloaded) message.body;
        logger.info("track id: {}, url: {}, object store: {}", track.trackId(), track.trackUrl(), track.minioPath());
    }

    private void handleRefreshTracks(Message message) {
        long playlistId = ((PlaylistRefreshMessage) message.body()).playlistId();
        List<Track> newTracks = this.playlistService.getAndPersistNewTracks(playlistId);
        newTracks.stream().forEach(this::publishTrackSearchCommand);
    }

    private void handleSearchSucceeded(Message message) {
        var searchResultMessage = (TrackSearchSucceeded) message.body();
        var results = searchResultMessage.results();
        var max = results.stream().mapToInt(r -> r.score()).max();
        if (max.isPresent()) {
            var matches = results.stream().filter(r -> r.score() == max.getAsInt()).toList();
            if (matches.size() == 1) {
                playlistService.updateTrackStatus(searchResultMessage.trackId(), TrackStatus.READY_TO_DOWNLOAD);
                publishTrackYtDownload(searchResultMessage.playlistId(), searchResultMessage.trackId(), matches.get(0));
            } else {
                // TODO: save results to DB
                //playlistService.updateTrackStatus(searchResultMessage.trackId(), TrackStatus.SEARCH_REVIEW);
                playlistService.updateTrackStatus(searchResultMessage.trackId(), TrackStatus.READY_TO_DOWNLOAD);
                // an admin will select the one to download
                publishTrackYtDownload(searchResultMessage.playlistId(), searchResultMessage.trackId(), matches.get(0));
            }
        }
    }

    private void handleSearchFailed(Message message) {

    }

    private void handleYtDownloadSucceeded(Message message) {
        var downloadResultMessage = (TrackDownloadSucceeded) message.body();
        playlistService.updateTrackStatus(downloadResultMessage.trackId(), TrackStatus.DOWNLOADED);
        // TODO: should send message for lyrics and equalizer
        playlistService.updateTrackStatus(downloadResultMessage.trackId(), TrackStatus.READY_FOR_PLAYER);
    }

    private void handleYtDownloadFailed(Message message) {
        var downloadResultMessage = (TrackDownloadFailed) message.body();
        logger.info("Failed to download {} for playlist {}", downloadResultMessage.trackId(), downloadResultMessage.playlistId());
        //playlistService.updateTrackStatus(downloadResultMessage.trackId(), TrackStatus.DOWNLOADED_FAILED);
        
    }

    private void publishTrackSearchCommand(Track track) {
        var command = TrackSearchCommand.of(track);
        publisherService.publishMessage(Topics.DZ_PLAYLIST_COMMANDS, command);
    }

    private void publishTrackYtDownload(long playlistId, long trackId, SearchResultDto result) {
        var command = new TrackYtDownloadCommand(playlistId, trackId, result.id());
        publisherService.publishMessage(Topics.DZ_PLAYLIST_COMMANDS, command);
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
