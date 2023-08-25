package com.queryinterface.radio.playlist.kafka.messages;

public record TrackYtDownloadCommand(long playlistId, long trackId, String videoId) implements KafkaMessage {
    
    @Override
    public String getType() {
        return "track.yt.download.command.v1";
    }
}
