package com.moe.socialnetwork.common.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moe.socialnetwork.common.models.Audio;

public interface AudioJpa extends JpaRepository<Audio, Long> {
    @Query("SELECT a FROM Audio a Join a.ownerPost p WHERE p.isDeleted = false AND a.ownerPost.id = :postId")
    Audio findAudioByOwnerPostId(Long postId);
}
