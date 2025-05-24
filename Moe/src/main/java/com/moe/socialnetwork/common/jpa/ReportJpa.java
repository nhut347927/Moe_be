package com.moe.socialnetwork.common.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Report;

public interface ReportJpa extends JpaRepository<Report, Long>{

}
