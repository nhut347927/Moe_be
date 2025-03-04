package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.PostPlaylist;

public interface PostStoryJPA extends JpaRepository<PostPlaylist, Long> {

}
