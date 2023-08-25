package com.queryinterface.radio.playlist.repositories;

import org.springframework.data.repository.ListCrudRepository;

import com.queryinterface.radio.playlist.Playlist;

public interface PlaylistRepository extends ListCrudRepository<Playlist, Long> {

}
