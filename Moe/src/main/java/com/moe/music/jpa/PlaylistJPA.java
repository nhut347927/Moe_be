package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.Playlist;

public interface PlaylistJPA extends JpaRepository<Playlist, Integer>{

}
