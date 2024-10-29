package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.Post;

public interface PostJPA extends JpaRepository<Post, Integer>{

}
