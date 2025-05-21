package com.moe.socialnetwork.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Follower;

public interface FollowerRepository extends JpaRepository<Follower, Long>{

}
