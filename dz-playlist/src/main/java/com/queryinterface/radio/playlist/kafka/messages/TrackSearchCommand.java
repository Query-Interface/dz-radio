package com.queryinterface.radio.playlist.kafka.messages;

import com.queryinterface.radio.playlist.Track;

public record TrackSearchCommand(long playlistId,
                              long trackId,
                              String title,
                              String artist) implements KafkaMessage {
    
    public static TrackSearchCommand of(Track track) {
        return new TrackSearchCommand(track.getPlaylistId(), track.getId(), track.getName(), track.getArtist());
    }

    @Override
    public String getType() {
        return "track.search.v1";
    }
}
