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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer userId;

	@Column(nullable = false, unique = true, length = 100)
	@Email(message = "Email should be valid")
	private String email;

	@Column(name = "password_hash", nullable = false, length = 255)
	@NotNull(message = "Password hash cannot be null")
	@NotBlank(message = "Password hash cannot be empty")
	private String passwordHash;

	@Column(name = "display_name", length = 100)
	@Size(max = 100, message = "Display name must not exceed 100 characters")
	private String displayName;

	@Column(name = "profile_picture_url", length = 255)
	private String profilePictureUrl;

	@Column(length = 255)
	private String bio;

	@Column(name = "website_url", length = 255)
	private String websiteUrl;

	@Column(length = 100)
	private String location;

	@Column(name = "date_of_birth")
	private LocalDateTime dateOfBirth;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	@Column(name = "is_verified", columnDefinition = "boolean default false")
	private Boolean isVerified = false;

	@Column(name = "is_active", columnDefinition = "boolean default true")
	private Boolean isActive = true;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "last_login")
	private LocalDateTime lastLogin;

	@Column(name = "last_activity")
	private LocalDateTime lastActivity;

	@Column(name = "refresh_token", length = 255)
	private String refreshToken;
	
	@Column(name = "refresh_token_expires")
	private LocalDateTime refreshTokenExpires; 

	@Column(name = "password_reset_token", length = 255)
	private String passwordResetToken;

	@Column(name = "password_reset_expires")
	private LocalDateTime passwordResetExpires;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
	
	@ManyToOne
	@JoinColumn(name = "roleId", nullable = false)
	@NotNull(message = "Role ID cannot be null")
	@JsonBackReference
	private Role role;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Post> posts;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<PostComment> postComments;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<PostLike> postLikes;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Reel> reels;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<ReelComment> reelComments;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<ReelLike> reelLikes;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<ActivityLog> activityLogs;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Feedback> feedbacks;

	@OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Follower> followers;

	@OneToMany(mappedBy = "followed", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Follower> followeds;

	@OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Message> senders;

	@OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Message> receivers;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Notification> notifications;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Report> reports;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<SongLike> songLikes;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<UserPlaylist> userPlaylists;

	

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

	public enum Gender {
		MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
	}
}
