package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.Media;

public interface MediaJPA extends JpaRepository<Media, Integer> {

}
