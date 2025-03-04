package com.moe.music.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.moe.music.model.User;

public interface UserJPA extends JpaRepository<User, Long> {
	@Query("SELECT u FROM User u WHERE u.email = :email")
	Optional<User> findByEmail(@Param("email") String email);
	
	@Query("SELECT u FROM User u WHERE u.displayName = :displayName")
	Optional<User> findByDisplayName(@Param("displayName") String DisplayName);

	@Query("SELECT u FROM User u WHERE u.passwordResetToken = :passwordResetToken")
	Optional<User> findByPasswordResetToken(@Param("passwordResetToken") String passwordResetToken);
	
	@Query("SELECT u FROM User u WHERE u.refreshToken = :refreshToken")
	Optional<User> findByrefreshToken(@Param("refreshToken") String refreshToken);
}
