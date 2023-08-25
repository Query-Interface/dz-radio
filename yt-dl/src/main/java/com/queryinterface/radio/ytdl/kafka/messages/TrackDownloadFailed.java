package com.queryinterface.radio.ytdl.kafka.messages;



public record TrackDownloadFailed(long playlistId, long trackId) implements KafkaMessage {
    
    @Override
    public String getType() {
        return "track.yt.download.failed.v1";
    }
}
