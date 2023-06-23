package com.queryinterface.deezer.playlist;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlaylistController {
    private PlaylistService playlistService;

    public PlaylistController(PlaylistService service) {
        this.playlistService = service;
    }

    @GetMapping(path = "/api/tracks/{playlistId}")
    public List<Track> getTracks(@PathVariable(name = "playlistId") long playlistId) {
        return playlistService.getTracks(playlistId);
    }
}
