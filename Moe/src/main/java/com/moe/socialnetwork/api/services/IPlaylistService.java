package com.moe.socialnetwork.api.services;

import com.moe.socialnetwork.api.dtos.PlaylistDTO;
import com.moe.socialnetwork.api.dtos.PlaylistPostDTO;
import com.moe.socialnetwork.api.dtos.PlaylistSaveDTO;
import com.moe.socialnetwork.common.models.User;

import java.util.List;
import java.util.UUID;

public interface IPlaylistService {
    List<PlaylistDTO> getPlaylistsByUser(UUID userCode);
    List<PlaylistPostDTO> getPostsByPlaylist(UUID playlistCode, User user);
    void createPlaylist(PlaylistSaveDTO dto, User user);
    void updatePlaylist(PlaylistDTO dto, User user);
    void addPostToPlaylist(User user, UUID playlistCode, UUID postCode);
    void removePostFromPlaylist(User user, UUID playlistCode, UUID postCode);
}