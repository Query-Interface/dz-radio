package com.queryinterface.radio.playlist.kafka.messages;

public record TrackSearchFailed(long playlistId, long trackId, String error) implements KafkaMessage {
    
    @Override
    public String getType() {
        return "track.search.failed.v1";
    }
}
