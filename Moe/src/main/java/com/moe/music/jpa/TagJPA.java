package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.Tag;

public interface TagJPA extends JpaRepository<Tag, Integer> {

}
