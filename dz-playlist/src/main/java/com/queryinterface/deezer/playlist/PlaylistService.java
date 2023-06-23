package com.queryinterface.deezer.playlist;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.queryinterface.deezer.playlist.dto.PlaylistDto;
import com.queryinterface.deezer.playlist.dto.TrackDto;

import reactor.core.publisher.Mono;

@Service
public class PlaylistService {
    private final String BASE_URL = "https://api.deezer.com/playlist";
    private WebClient webClient;

    public PlaylistService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
    }

    public List<Track> getTracks(final long playlistId) {
        Mono<PlaylistDto> playlist = this.webClient.get()
            .uri("/"+playlistId)
            .retrieve()
            .bodyToMono(PlaylistDto.class);
        Stream<TrackDto> trackStream = playlist.blockOptional().get().getTracks().getData().stream();
        List<Track> tracks = trackStream.map(t -> {
            var track = new Track();
            track.setId(t.getId());
            track.setName(t.getTitle());
            track.setArtist(t.getArtist().getName());
            track.setDuration(t.getDuration());
            track.setCover(t.getAlbum().getCover());
            return track;
        }).toList();
        return tracks;
    }
}
