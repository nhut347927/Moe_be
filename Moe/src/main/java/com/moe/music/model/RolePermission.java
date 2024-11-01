package com.moe.music.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Role_Permissions")
public class RolePermission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer rolePermissionId;

	@ManyToOne
	@JoinColumn(name = "role_id", nullable = false)
	@JsonBackReference
	private Role role; // Mối quan hệ với bảng Roles

	@ManyToOne
	@JoinColumn(name = "permission_id", nullable = false)
	@JsonBackReference
	private Permission permission; // Mối quan hệ với bảng Permissions
}
