package com.moe.socialnetwork.common.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.moe.socialnetwork.common.models.RolePermission;

public interface RolePermissionJpa extends JpaRepository<RolePermission, Long> {
	@Query("SELECT rp FROM RolePermission rp JOIN rp.user u WHERE u.id = :userId")
	List<RolePermission> findRolePermissionsByUserId(@Param("userId") Long userId);
}