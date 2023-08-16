package com.queryinterface.dz.playlist;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Track {
    @Id
    private long id;
    private String name;
    private String artist;
    private int duration;
    private String cover;
    private boolean explicit = false;
    private long playlistId;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getArtist() {
        return artist;
    }
    public void setArtist(String artist) {
        this.artist = artist;
    }
    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    public String getCover() {
        return cover;
    }
    public void setCover(String cover) {
        this.cover = cover;
    }
    public boolean isExplicit() {
        return this.explicit;
    }
    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }
    public long getPlaylistId() {
        return playlistId;
    }
    public void setPlaylistId(long playlistId) {
        this.playlistId = playlistId;
    }
}
