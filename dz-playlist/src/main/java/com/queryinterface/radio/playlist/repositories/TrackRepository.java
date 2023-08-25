package com.queryinterface.radio.playlist.repositories;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import com.queryinterface.radio.playlist.Track;

public interface TrackRepository extends ListCrudRepository<Track, Long> {
    List<Track> findTracksByPlaylistId(long playlistId);
}
