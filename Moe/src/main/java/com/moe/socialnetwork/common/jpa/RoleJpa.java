package com.moe.socialnetwork.common.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Role;

public interface RoleJpa extends JpaRepository<Role, Long> {

}
