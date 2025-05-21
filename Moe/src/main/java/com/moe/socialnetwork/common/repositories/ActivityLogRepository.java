package com.moe.socialnetwork.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.ActivityLog;



public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long>{

}
