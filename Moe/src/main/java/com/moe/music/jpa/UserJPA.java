package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.moe.music.model.User;

public interface UserJPA extends JpaRepository<User, Integer> {
	@Query("SELECT u FROM User u WHERE u.email = :email")
	User findByEmail(@Param("email") String email);

}
