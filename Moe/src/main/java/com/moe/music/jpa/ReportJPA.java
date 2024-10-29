package com.moe.music.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.music.model.Report;

public interface ReportJPA extends JpaRepository<Report, Integer>{

}
