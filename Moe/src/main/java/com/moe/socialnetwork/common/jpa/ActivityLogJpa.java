package com.moe.socialnetwork.common.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.ActivityLog;



public interface ActivityLogJpa extends JpaRepository<ActivityLog, Long>{

}
