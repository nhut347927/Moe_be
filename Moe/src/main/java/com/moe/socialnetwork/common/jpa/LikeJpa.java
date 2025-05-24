package com.moe.socialnetwork.common.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Like;

public interface LikeJpa extends JpaRepository<Like, Long>{

}
