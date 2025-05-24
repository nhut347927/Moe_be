package com.moe.socialnetwork.common.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Comment;



public interface CommentJpa extends JpaRepository<Comment, Long> {

}
