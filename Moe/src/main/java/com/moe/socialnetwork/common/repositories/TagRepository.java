package com.moe.socialnetwork.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

}
