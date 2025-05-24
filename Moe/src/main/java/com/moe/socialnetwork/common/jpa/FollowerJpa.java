package com.moe.socialnetwork.common.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Follower;

public interface FollowerJpa extends JpaRepository<Follower, Long>{

}
