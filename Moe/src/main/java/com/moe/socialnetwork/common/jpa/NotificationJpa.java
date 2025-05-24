package com.moe.socialnetwork.common.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Notification;

public interface NotificationJpa extends JpaRepository<Notification, Long>{

}
