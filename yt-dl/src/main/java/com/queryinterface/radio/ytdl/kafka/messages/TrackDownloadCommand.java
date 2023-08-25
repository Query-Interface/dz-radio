package com.queryinterface.radio.ytdl.kafka.messages;

public record TrackDownloadCommand(long playlistId, long trackId, String videoId) implements KafkaMessage {
    
    @Override
    public String getType() {
        return "track.yt.download.command.v1";
    }
}
