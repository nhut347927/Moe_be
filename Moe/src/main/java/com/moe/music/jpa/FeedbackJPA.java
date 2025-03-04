package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.Feedback;

public interface FeedbackJPA extends JpaRepository<Feedback, Long>{

}
