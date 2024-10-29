package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.Follower;

public interface FollowerJPA extends JpaRepository<Follower, Integer>{

}
