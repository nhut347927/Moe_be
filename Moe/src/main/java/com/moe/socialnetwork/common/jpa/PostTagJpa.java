package com.moe.socialnetwork.common.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.PostTag;

public interface PostTagJpa extends JpaRepository<PostTag, Long> {

}
