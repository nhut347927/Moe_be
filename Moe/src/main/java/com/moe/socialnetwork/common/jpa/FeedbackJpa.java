package com.moe.socialnetwork.common.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Feedback;

public interface FeedbackJpa extends JpaRepository<Feedback, Long>{

}
