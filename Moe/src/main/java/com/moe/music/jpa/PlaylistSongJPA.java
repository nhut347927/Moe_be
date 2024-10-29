package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.PlaylistSong;

public interface PlaylistSongJPA extends JpaRepository<PlaylistSong, Integer>{

}
