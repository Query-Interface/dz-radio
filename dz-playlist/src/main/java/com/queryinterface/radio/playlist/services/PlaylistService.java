package com.queryinterface.radio.playlist.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.queryinterface.radio.playlist.dto.PlaylistDto;
import com.queryinterface.radio.playlist.dto.TrackDto;
import com.queryinterface.radio.playlist.repositories.PlaylistRepository;
import com.queryinterface.radio.playlist.repositories.TrackRepository;
import com.queryinterface.radio.playlist.Playlist;
import com.queryinterface.radio.playlist.Track;
import com.queryinterface.radio.playlist.TrackStatus;

import reactor.core.publisher.Mono;

@Service
public class PlaylistService {
    private final String BASE_URL = "https://api.deezer.com/playlist";
    private WebClient webClient;
    private TrackRepository trackRepository;
    private PlaylistRepository playlistRepository;

    public PlaylistService(WebClient.Builder webClientBuilder,
                           PlaylistRepository playlistRepository,
                           TrackRepository trackRepository) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        this.playlistRepository = playlistRepository;
        this.trackRepository = trackRepository;
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
            track.setExplicit(t.isExplicit());
            return track;
        }).toList();
        return tracks;
    }

    public Playlist getPlaylistFromDeezer(long playlistId) {
        Mono<PlaylistDto> playlistDto = this.webClient.get()
            .uri("/"+playlistId)
            .retrieve()
            .bodyToMono(PlaylistDto.class);
        Stream<TrackDto> trackStream = playlistDto.blockOptional().get().getTracks().getData().stream();
        List<Track> tracks = trackStream.map(t -> {
            var track = new Track();
            track.setId(t.getId());
            track.setName(t.getTitle());
            track.setArtist(t.getArtist().getName());
            track.setDuration(t.getDuration());
            track.setCover(t.getAlbum().getCover());
            track.setExplicit(t.isExplicit());
            return track;
        }).toList();
        Playlist playlist = new Playlist();
        playlist.setPlaylistId(playlistId);
        playlist.setName( playlistDto.blockOptional().get().getTitle());
        playlist.setTracks(tracks);
        return playlist;
    }

    public List<Track> getTracksFromDB(long playlistId) {
        return trackRepository.findTracksByPlaylistId(playlistId);
    }

    public List<Track> getAndPersistNewTracks(long playlistId) {
        List<Track> tracksInDB = this.getTracksFromDB(playlistId);
        var playlist = this.getPlaylistFromDeezer(playlistId);
        List<Track> tracksInDeezer = playlist.getTracks();
        Set<Long> knownTracks = tracksInDB.stream().map(t -> t.getId()).collect(Collectors.toSet());
        List<Track> newTracks = tracksInDeezer.stream().filter(t -> !knownTracks.contains(t.getId())).toList();

        ensurePlaylistExist(playlist);
        trackRepository.saveAll(newTracks);
        return newTracks;
    }

    public void ensurePlaylistExist(Playlist playlist) {
        Optional<Playlist> pl = playlistRepository.findById(Long.valueOf(playlist.getPlaylistId()));
        if (pl.isEmpty()) {
            playlistRepository.save(playlist);
        }
    }

    public void addTrack(Track track) {
        trackRepository.save(track);
    }

    public void updateTrackStatus(long trackId, TrackStatus status) {
        Optional<Track> optTrack = trackRepository.findById(trackId);
        if (optTrack.isPresent()) {
            var track = optTrack.get();
            track.setStatus(status);
            trackRepository.save(track);
        }
    }
}
