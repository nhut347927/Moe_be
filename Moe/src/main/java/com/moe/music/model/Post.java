package com.moe.music.model;

import java.time.LocalDateTime;
import java.util.List;

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
	private Integer postId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@JsonBackReference
	private User user;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(name = "video", length = 255)
	private String video;

	@Column(name = "audio", length = 255)
	private String audio;

	@Column(name = "view_count", columnDefinition = "int default 0")
	private Integer viewCount = 0;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "is_deleted", columnDefinition = "boolean default false")
	private Boolean isDeleted = false;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "audio_post_id")
	private Post ownerAudioPostId;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private PostType type;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<PostStory> postStories;

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

	public void softDelete() {
		this.deletedAt = LocalDateTime.now();
		this.isDeleted = true;
	}

	public void restore() {
		this.isDeleted = false;
	}

	public enum PostType {
		IMAGE, // Dành cho bài đăng là ảnh
		VIDEO, // Dành cho bài đăng là video
	}

}
