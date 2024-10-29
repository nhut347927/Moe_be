package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.Message;

public interface MessageJPA extends JpaRepository<Message, Integer>{

}
