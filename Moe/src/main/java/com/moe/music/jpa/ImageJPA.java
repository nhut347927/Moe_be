package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.Image;

public interface ImageJPA extends JpaRepository<Image, Long>{
    
}
