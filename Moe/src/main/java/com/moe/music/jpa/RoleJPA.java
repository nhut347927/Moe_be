package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.Role;

public interface RoleJPA extends JpaRepository<Role, Long> {

}
