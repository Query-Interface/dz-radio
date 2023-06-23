package com.queryinterface.deezer.playlist.dto;

public class TrackDto {
    private long id;
    private String title;
    private int duration;
    private ArtistDto artist;
    private AlbumDto album;

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

    
}
