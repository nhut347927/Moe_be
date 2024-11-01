package com.moe.music.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Permissions")
public class Permission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer permissionId;

	@Column(name = "module_name", nullable = false, length = 50)
	@NotNull(message = "Module name cannot be null")
	private String moduleName; // Ví dụ: 'Products', 'Users', 'Posts'

	@Column(name = "action_name", nullable = false, length = 50)
	@NotNull(message = "Action name cannot be null")
	private String actionName; // Ví dụ: 'Create', 'Read', 'Update', 'Delete'

	@Column(length = 255)
	private String description;

	@OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<RolePermission> rolePermissions;

	// Phương thức có thể thêm vào đây nếu cần, ví dụ như phương thức để kiểm tra
	// quyền
}
