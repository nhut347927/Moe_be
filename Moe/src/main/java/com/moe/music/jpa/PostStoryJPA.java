package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.PostStory;

public interface PostStoryJPA extends JpaRepository<PostStory, Integer> {

}
