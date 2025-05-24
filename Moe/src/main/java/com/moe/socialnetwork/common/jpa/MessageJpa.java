package com.moe.socialnetwork.common.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Message;

public interface MessageJpa extends JpaRepository<Message, Long>{

}
