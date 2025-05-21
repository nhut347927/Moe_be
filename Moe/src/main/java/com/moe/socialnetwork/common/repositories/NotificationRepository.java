package com.moe.socialnetwork.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long>{

}
