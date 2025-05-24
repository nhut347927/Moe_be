package com.moe.socialnetwork.common.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Tag;

public interface TagJpa extends JpaRepository<Tag, Long> {

}
