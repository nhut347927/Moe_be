package com.moe.music.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reportId;  // Khóa chính

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;  // Người thực hiện báo cáo

    @Column(name = "target_id", nullable = false)
    private Integer targetId;  // ID của mục tiêu được báo cáo

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;  // Loại mục tiêu (VD: "Post", "Song", "User")

    @Column(name = "reason", nullable = false)
    private String reason;  // Lý do báo cáo

    @Column(name = "status", nullable = false, length = 20)
    private String status = "pending";  // Trạng thái báo cáo (VD: "pending", "resolved", "rejected")

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;  // Thời gian tạo

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(); // Thiết lập thời gian tạo khi lưu
    }
}
