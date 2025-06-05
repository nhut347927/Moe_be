package com.moe.socialnetwork.common.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.moe.socialnetwork.common.models.User;

public interface UserJpa extends JpaRepository<User, Long> {
	@Query("SELECT u FROM User u WHERE u.email = :email")
	Optional<User> findByEmail(@Param("email") String email);
	
	@Query("SELECT u FROM User u WHERE u.userName = :userName")
	Optional<User> findByUserName(@Param("userName") String userName);

	@Query("SELECT u FROM User u WHERE u.passwordResetToken = :passwordResetToken")
	Optional<User> findByPasswordResetToken(@Param("passwordResetToken") String passwordResetToken);

	@Query("SELECT u FROM User u WHERE u.code = :code AND u.isDeleted = false")
	Optional<User> findByCode(String code);
}
