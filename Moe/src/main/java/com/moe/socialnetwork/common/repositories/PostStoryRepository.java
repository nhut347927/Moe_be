package com.moe.socialnetwork.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.PostPlaylist;

public interface PostStoryRepository extends JpaRepository<PostPlaylist, Long> {

}
