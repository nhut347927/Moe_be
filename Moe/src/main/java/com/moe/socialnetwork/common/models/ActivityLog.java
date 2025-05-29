package com.moe.socialnetwork.common.models;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

/**
 * Author: nhut379
 */
@Entity
@Table(name = "ActivityLogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, updatable = false)
	private UUID code;

	@Column(name = "message", length = 255)
	private String message;	

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "data", columnDefinition = "TEXT")
	private String data;

	@Column(name = "error", columnDefinition = "TEXT")
	private String error;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", insertable = false, updatable = false)
	@JsonBackReference
	private User user;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.code = UUID.randomUUID();

	}

	public enum LogActionType {
		ADD, UPDATE, DELETE, VIEW, LIKE, UNLIKE, FOLLOW, UNFOLLOW, BLOCK, UNBLOCK, SHARE, REPORT, COMMENT
	}

	public enum LogTargetType {
		POST, COMMENT, COMMENT2, LIKE, LIKECOMENT, LIKECOMENT2, MESSAGE, PERMISSION, LOGIN, REGISTER, STORY, REPORT,
		SETTING
	}
}
