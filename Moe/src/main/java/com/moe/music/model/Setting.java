package com.moe.music.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Settings")
public class Setting {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer settingId; // Khóa chính

	@Column(name = "setting_key", nullable = false, unique = true, length = 100)
	private String settingKey; // Khóa cấu hình (VD: "app_name", "theme")

	@Column(length = 255)
	private String value; // Giá trị cấu hình

	@Column(name = "description")
	private String description; // Mô tả về cấu hình

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt; // Thời gian tạo

	@Column(name = "updated_at")
	private LocalDateTime updatedAt; // Thời gian cập nhật

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
