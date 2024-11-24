package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.Comment;

public interface CommentJPA extends JpaRepository<Comment, Integer> {

}
