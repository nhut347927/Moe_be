package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.Setting;

public interface SettingJPA extends JpaRepository<Setting, Integer>{

}
