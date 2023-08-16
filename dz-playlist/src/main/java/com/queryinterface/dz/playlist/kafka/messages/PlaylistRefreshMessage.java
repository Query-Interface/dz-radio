package com.queryinterface.dz.playlist.kafka.messages;

public record PlaylistRefreshMessage(long playlistId) implements KafkaMessage {

    @Override
    public String getType() {
        return "playlist.refresh.v1";
    }
}
