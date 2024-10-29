package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.Song;

public interface SongJPA extends JpaRepository<Song, Integer>{

}
