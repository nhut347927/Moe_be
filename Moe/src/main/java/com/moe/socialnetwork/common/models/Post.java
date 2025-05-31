package com.moe.socialnetwork.common.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: nhut379
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Posts")
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, updatable = false)
	private UUID code;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@JsonBackReference
	private User user;

	@Column(length = 255)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "video_url", length = 255)
	private String videoUrl;

	@Column(name = "video_thumbnail", length = 255)
	private String videoThumbnail;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "audio_id")
	@JsonBackReference(value = "audio-post")
	private Audio audio;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private PostType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "visibility", nullable = false)
	private Visibility visibility;

	@Column(name = "is_deleted", columnDefinition = "boolean default false")
	private Boolean isDeleted = false;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_create", insertable = false, updatable = false)
	@JsonBackReference
	private User userCreate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_update", insertable = false, updatable = false)
	@JsonBackReference
	private User userUpdate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_delete", insertable = false, updatable = false)
	@JsonBackReference
	private User userDelete;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Image> images;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Comment> comments;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Like> likes;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<PostTag> postTags;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<View> views;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<PostPlaylist> postPlaylists;

	public void softDelete() {
		this.deletedAt = LocalDateTime.now();
		this.isDeleted = true;
	}

	public void restore() {
		this.isDeleted = false;
	}

	@PrePersist
	protected void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
		this.code = UUID.randomUUID();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public enum Visibility {
		PRIVATE,
		PUBLIC
	}

	public enum PostType {
		IMG, // bài đăng là ảnh
		VID, // đăng là video
	}

}
