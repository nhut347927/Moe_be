package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.User;

public interface UserJPA extends JpaRepository<User, Integer>{
	   User findByUsername(String username);
}
