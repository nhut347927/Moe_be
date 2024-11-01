package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.PostComment;

public interface PostCommentJPA extends JpaRepository<PostComment, Integer> {

}
