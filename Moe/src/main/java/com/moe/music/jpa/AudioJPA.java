package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moe.music.model.Audio;

public interface AudioJPA extends JpaRepository<Audio, Long> {
    @Query("SELECT a FROM Audio a Join a.ownerPost p WHERE p.isDeleted = false AND a.ownerPost.id = :postId")
    Audio findAudioByOwnerPostId(Long postId);
}
