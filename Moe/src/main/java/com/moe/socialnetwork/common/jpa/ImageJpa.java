package com.moe.socialnetwork.common.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Image;

public interface ImageJpa extends JpaRepository<Image, Long>{
    
}
