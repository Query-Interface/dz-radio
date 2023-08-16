package com.queryinterface.dz.playlist.repositories;

import org.springframework.data.repository.ListCrudRepository;

import com.queryinterface.dz.playlist.Playlist;

public interface PlaylistRepository extends ListCrudRepository<Playlist, Long> {

}
