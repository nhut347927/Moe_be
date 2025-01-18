package com.moe.music.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.moe.music.model.RolePermission;

public interface RolePermissionJPA extends JpaRepository<RolePermission, Integer> {
	@Query("SELECT rp FROM RolePermission rp JOIN rp.user u WHERE u.id = :userId")
	List<RolePermission> findRolePermissionsByUserId(@Param("userId") Long userId);
}