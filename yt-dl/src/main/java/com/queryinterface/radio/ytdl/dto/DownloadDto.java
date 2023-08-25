package com.queryinterface.radio.ytdl.dto;

public record DownloadDto (long playlistId, long trackId, boolean isSuccess, String bucket, String fileName) {
    
}
