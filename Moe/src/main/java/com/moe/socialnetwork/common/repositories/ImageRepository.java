package com.moe.socialnetwork.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Image;

public interface ImageRepository extends JpaRepository<Image, Long>{
    
}
