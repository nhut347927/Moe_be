package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.Notification;

public interface NotificationJPA extends JpaRepository<Notification, Integer>{

}
