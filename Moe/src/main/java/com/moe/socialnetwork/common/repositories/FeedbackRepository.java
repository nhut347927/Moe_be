package com.moe.socialnetwork.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long>{

}
