package com.moe.music.model;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Songs")
public class Song {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer songId;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(length = 50)
	private String genre;

	@Column(nullable = false)
	private Short duration; // Sử dụng Short cho giá trị nhỏ

	@Column(name = "cover_image_url", length = 255)
	private String coverImageUrl;

	@Column(name = "cover_video_url", length = 255)
	private String coverVideoUrl;

	@Column(columnDefinition = "TEXT")
	private String lyrics;

	@Column(name = "file_url", nullable = false, length = 255)
	private String fileUrl;

	@Column(name = "view_count", columnDefinition = "int default 0")
	private Integer viewCount = 0;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@OneToMany(mappedBy = "song")
	@JsonManagedReference
	private List<PlaylistSong> playlistSongs;

	@OneToMany(mappedBy = "song")
	@JsonManagedReference
	private List<Post> posts;

	@OneToMany(mappedBy = "song")
	@JsonManagedReference
	private List<Reel> reels;

	@OneToMany(mappedBy = "song")
	@JsonManagedReference
	private List<SongLike> songLikes;

	@PrePersist
	protected void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
