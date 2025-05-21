package com.moe.socialnetwork.common.models;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.moe.socialnetwork.util.AuthorityUtil;

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
@Table(name = "Users")
public class User implements UserDetails {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, updatable = false)
	private UUID code;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(name = "password_hash", length = 255)
	private String passwordHash;

	@Column(name = "user_name", nullable = false, unique = true, length = 50)
	private String userName;

	@Column(name = "display_name", length = 100)
	private String displayName;

	@Column(name = "background_image", length = 255)
	private String backgroundImage;

	@Column(length = 255)
	private String bio;

	@Column(name = "provider")
	private String provider;

	@Column(length = 100)
	private String location;

	@Column(length = 255)
	private String avatar;

	@Column(name = "date_of_birth")
	private LocalDateTime dateOfBirth;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	@Column(name = "is_verified", columnDefinition = "boolean default false")
	private Boolean isVerified = false;

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

	@Column(name = "last_login")
	private LocalDateTime lastLogin;

	@Column(name = "password_reset_token", length = 255)
	private String passwordResetToken;

	@Column(name = "password_reset_expires")
	private LocalDateTime passwordResetExpires;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Post> posts;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Comment> postComments;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Like> postLikes;

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
	private List<UserPlaylist> userStories;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Tag> tags;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<View> views;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<CommentLike> commentLikes;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JsonManagedReference
	private List<RolePermission> rolePermissions;

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
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	@PrePersist
	public void prePersist() {
		if (code == null) {
			code = UUID.randomUUID();
		}
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		Set<String> authoritySet = AuthorityUtil.convertToAuthorities(rolePermissions);

		return authoritySet.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	public enum Gender {
		MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
	}
}
