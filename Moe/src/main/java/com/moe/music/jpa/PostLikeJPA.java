package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.PostLike;

public interface PostLikeJPA extends JpaRepository<PostLike, Integer>{

}
