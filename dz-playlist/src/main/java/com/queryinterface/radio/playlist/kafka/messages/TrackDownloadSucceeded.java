package com.queryinterface.radio.playlist.kafka.messages;

public record TrackDownloadSucceeded(long playlistId, long trackId, String bucket, String fileName) implements KafkaMessage {
    
    @Override
    public String getType() {
        return "track.yt.download.succeeded.v1";
    }
}
