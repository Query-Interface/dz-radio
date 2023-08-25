package com.queryinterface.radio.playlist.kafka.messages;

public record TrackDownloaded(String trackId, String trackUrl, String minioPath) implements KafkaMessage {
    @Override
    public String getType() {
        return "track.downloaded.v1";
    }
}
