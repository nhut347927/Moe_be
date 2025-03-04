package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.Like;

public interface LikeJPA extends JpaRepository<Like, Long>{

}
