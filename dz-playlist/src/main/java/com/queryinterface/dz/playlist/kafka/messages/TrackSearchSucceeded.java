package com.queryinterface.dz.playlist.kafka.messages;

import java.util.List;

import com.queryinterface.dz.playlist.dto.SearchResultDto;

public record TrackSearchSucceeded(long playlistId, long trackId, List<SearchResultDto> results) implements KafkaMessage {
    
    @Override
    public String getType() {
        return "track.search.succeeded.v1";
    }
}
