package com.queryinterface.radio.playlist;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

@Entity
public class Playlist {

    @Id
    private long playlistId;
    private String name;
    @Transient
    private List<Track> tracks;

    public long getPlaylistId() {
        return playlistId;
    }
    public void setPlaylistId(long playlistId) {
        this.playlistId = playlistId;
    }
    public List<Track> getTracks() {
        return tracks;
    }
    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

}
