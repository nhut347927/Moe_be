package com.moe.music.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Playlist_Songs")
public class PlaylistSong {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer playlistSongId;

	@ManyToOne
	@JoinColumn(name = "playlist_id", nullable = false)
	@JsonBackReference
	private Playlist playlist;

	@ManyToOne
	@JoinColumn(name = "song_id", nullable = false)
	@JsonBackReference
	private Song song;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}
