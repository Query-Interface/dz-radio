package com.queryinterface.radio.playlist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlaylistDto {
    private long id;
    private String title;
    private String link;
    private String picture;
    @JsonProperty("picture_small")
    private String pictureSmall;
    @JsonProperty("picture_medium")
    private String pictureMedium;
    @JsonProperty("picture_big")
    private String pictureBig;
    private TracksDto tracks;
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
    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }
    public String getPicture() {
        return picture;
    }
    public void setPicture(String picture) {
        this.picture = picture;
    }
    public String getPictureSmall() {
        return pictureSmall;
    }
    public void setPictureSmall(String pictureSmall) {
        this.pictureSmall = pictureSmall;
    }
    public String getPictureMedium() {
        return pictureMedium;
    }
    public void setPictureMedium(String pictureMedium) {
        this.pictureMedium = pictureMedium;
    }
    public String getPictureBig() {
        return pictureBig;
    }
    public void setPictureBig(String pictureBig) {
        this.pictureBig = pictureBig;
    }
    public TracksDto getTracks() {
        return tracks;
    }
    public void setTracks(TracksDto tracks) {
        this.tracks = tracks;
    }

    
}