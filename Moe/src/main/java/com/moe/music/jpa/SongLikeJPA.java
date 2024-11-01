package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.SongLike;

public interface SongLikeJPA extends JpaRepository<SongLike, Integer>{

}
