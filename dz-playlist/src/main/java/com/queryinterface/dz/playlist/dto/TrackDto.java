package com.queryinterface.dz.playlist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrackDto {
    private long id;
    private String title;
    private int duration;
    private ArtistDto artist;
    private AlbumDto album;
    private boolean explicit;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    public ArtistDto getArtist() {
        return artist;
    }
    public void setArtist(ArtistDto artist) {
        this.artist = artist;
    }
    public AlbumDto getAlbum() {
        return album;
    }
    public void setAlbum(AlbumDto album) {
        this.album = album;
    }
    @JsonProperty("explicit_lyrics")
    public boolean isExplicit() {
        return explicit;
    }
    public void setExplicit(boolean isEexplit) {
        this.explicit = isEexplit;
    }
}
