package com.moe.socialnetwork.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Comment;



public interface CommentRepository extends JpaRepository<Comment, Long> {

}
