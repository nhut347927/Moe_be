package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.ActivityLog;

public interface ActivityLogJPA extends JpaRepository<ActivityLog, Long>{

}
