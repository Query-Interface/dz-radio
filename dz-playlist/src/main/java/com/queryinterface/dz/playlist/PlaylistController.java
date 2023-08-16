package com.queryinterface.dz.playlist;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.queryinterface.dz.playlist.dto.TrackDto;
import com.queryinterface.dz.playlist.kafka.Topics;
import com.queryinterface.dz.playlist.kafka.messages.TrackSearchCommand;
import com.queryinterface.dz.playlist.services.KafkaPublisherService;
import com.queryinterface.dz.playlist.services.PlaylistService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class PlaylistController {
    private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class);

    private PlaylistService playlistService;
    private KafkaPublisherService publisherService;

    public PlaylistController(PlaylistService service, KafkaPublisherService publisherService) {
        this.playlistService = service;
        this.publisherService = publisherService;
    }

    @GetMapping(path = "/api/tracks/{playlistId}")
    public List<Track> getTracks(@PathVariable(name = "playlistId") long playlistId) {
        return playlistService.getTracks(playlistId);
    }

    @PostMapping(path = "api/test/tracks/")
    public void addTrack(@RequestBody TrackDto trackDto) {
        Track track = new Track();
        track.setPlaylistId(112);
        track.setId(trackDto.getId());
        track.setName(trackDto.getTitle());
        track.setArtist(trackDto.getArtist().getName());
        track.setCover(trackDto.getAlbum().getCover());
        track.setDuration(trackDto.getDuration());
        trackDto.setExplicit(false);
        playlistService.addTrack(track);
        publisherService.publishMessage(Topics.DZ_PLAYLIST_EVENTS, TrackSearchCommand.of(track));
    }

}
