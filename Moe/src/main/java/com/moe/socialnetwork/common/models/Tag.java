package com.moe.socialnetwork.common.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
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
@Table(name = "Tags")
public class Tag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, updatable = false)
	private UUID code;

	@Column(nullable = false, unique = true, length = 50)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_create", insertable = false, updatable = false)
	@JsonBackReference
	private User userCreate;

	@OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<PostTag> postTags;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "usage_count", nullable = false)
	private int usageCount = 0;

	@PrePersist
	protected void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.code = UUID.randomUUID();
	}

	public void incrementUsageCount() {
		this.usageCount++;
	}
}
