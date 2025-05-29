package com.moe.socialnetwork.common.jpa;

// Removed incorrect import. Use java.util.Optional instead.
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moe.socialnetwork.common.models.Tag;

public interface TagJpa extends JpaRepository<Tag, Long> {
    
    @Query("SELECT t FROM Tag t WHERE t.name = :name")
    Optional<Tag> findByName(String name);
  

}
