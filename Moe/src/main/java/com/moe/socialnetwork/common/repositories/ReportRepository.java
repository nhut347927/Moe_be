package com.moe.socialnetwork.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moe.socialnetwork.common.models.Report;

public interface ReportRepository extends JpaRepository<Report, Long>{

}
