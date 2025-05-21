package com.moe.socialnetwork.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

}
