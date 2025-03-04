package com.moe.music.model;

import java.time.LocalDateTime;

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
@Table(name = "RolePermissions")
public class RolePermission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false)
	@JsonBackReference
	private User user;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "role_id", nullable = false)
	@JsonBackReference
	private Role role;

	@Column(nullable = false, length = 255)
	private String moduleName;

	@Column(nullable = false)
	private Boolean canView = false;

	@Column(nullable = false)
	private Boolean canInsert = false;

	@Column(nullable = false)
	private Boolean canUpdate = false;

	@Column(nullable = false)
	private Boolean canDelete = false;

	@Column(nullable = false)
	private Boolean canRestore = false;

	

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_create", insertable = false, updatable = false)
	@JsonBackReference
	private User userCreate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_update", insertable = false, updatable = false)
	@JsonBackReference
	private User userUpdate;

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
