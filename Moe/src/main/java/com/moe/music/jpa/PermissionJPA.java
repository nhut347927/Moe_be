package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.Permission;

public interface PermissionJPA extends JpaRepository<Permission, Integer>{

}
