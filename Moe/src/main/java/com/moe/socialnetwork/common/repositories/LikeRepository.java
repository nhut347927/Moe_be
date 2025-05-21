package com.moe.socialnetwork.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Like;

public interface LikeRepository extends JpaRepository<Like, Long>{

}
