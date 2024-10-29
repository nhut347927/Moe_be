package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.PostTag;
import com.moe.music.model.PostTag.PostTagId;

public interface PostTagJPA extends JpaRepository<PostTag, PostTagId> {
}
