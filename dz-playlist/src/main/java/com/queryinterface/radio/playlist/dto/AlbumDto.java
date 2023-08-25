package com.queryinterface.radio.playlist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AlbumDto {
    private long id;
    private String title;
    private String cover;
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
    @JsonProperty("cover_medium")
    public String getCover() {
        return cover;
    }
    public void setCover(String cover) {
        this.cover = cover;
    }

    
}
