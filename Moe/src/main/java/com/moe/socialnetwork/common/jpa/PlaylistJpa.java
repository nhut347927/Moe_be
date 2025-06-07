package com.moe.socialnetwork.common.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moe.socialnetwork.common.models.Playlist;
import com.moe.socialnetwork.common.models.Post;

public interface PlaylistJpa extends JpaRepository<Playlist, Long> {
    @Query("SELECT p FROM UserPlaylist up JOIN up.user u JOIN up.playlist p WHERE u.code = :userCode AND p.isDeleted = false AND (p.isPublic = true OR p.user.code = :userCode)")
    List<Playlist> findPlaylistByUserCode(UUID userCode);

    @Query("SELECT po FROM PostPlaylist pl JOIN pl.post po JOIN pl.playlist p JOIN p.user u WHERE (p.isPublic = true OR u.id = :userId) AND p.code = :playlistCode")
    List<Post> findPostsByPlaylistCodeAndUserId(UUID playlistCode, Long userId);

    @Query("SELECT p FROM Playlist p JOIN p.user u WHERE p.code = :playlistCode AND (p.isPublic = true OR u.id = :userId)")
    Optional<Playlist> findPlaylistByPlaylistCodeAndUserId(UUID playlistCode, Long userId);
}
