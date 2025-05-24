package com.moe.socialnetwork.common.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.PostPlaylist;

public interface PostStoryJpa extends JpaRepository<PostPlaylist, Long> {

}
