package com.queryinterface.dz.playlist.kafka.messages;

public record TrackDownloaded(String trackId, String trackUrl, String minioPath) implements KafkaMessage {
    @Override
    public String getType() {
        return "track.downloaded.v1";
    }
}
