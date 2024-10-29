package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.UserPlaylist;

public interface UserPlaylistJPA extends JpaRepository<UserPlaylist, Integer>{

}
