package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.RolePermission;

public interface RolePermissionJPA extends JpaRepository<RolePermission, Integer>{

}
