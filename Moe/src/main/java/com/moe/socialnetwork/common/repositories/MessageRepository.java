package com.moe.socialnetwork.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Message;

public interface MessageRepository extends JpaRepository<Message, Long>{

}
