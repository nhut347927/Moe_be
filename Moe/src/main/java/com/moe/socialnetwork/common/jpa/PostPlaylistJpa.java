package com.moe.socialnetwork.common.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moe.socialnetwork.common.models.PostPlaylist;

public interface PostPlaylistJpa extends JpaRepository<PostPlaylist, Long> {
    @Query("SELECT pl FROM PostPlaylist pl WHERE pl.playlist.id = :playlistId AND pl.post.id = :postId")
    Optional<PostPlaylist> findPostPlaylistByPlaylistIdAndPostId(Long playlistId, Long postId);
}
