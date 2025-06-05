package com.moe.socialnetwork.common.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.moe.socialnetwork.common.models.RolePermission;

public interface RolePermissionJpa extends JpaRepository<RolePermission, Long> {
	@Query("SELECT rp FROM RolePermission rp JOIN rp.user u WHERE u.id = :userId")
	List<RolePermission> findRolePermissionsByUserId(@Param("userId") Long userId);

	@Query("SELECT rp FROM RolePermission rp JOIN rp.user u WHERE u.code = :code")
	List<RolePermission> findByUserCode(String code);

	// viết phương thức delete permisson theo code của user
	@Query("DeLETE FROM RolePermission rp WHERE rp.user.code = :code")
	void deleteByUserCode(@Param("code") String code);
}