package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.UserRole;

public interface UserRoleJPA extends JpaRepository<UserRole, Integer>{

}
