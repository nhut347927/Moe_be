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
@Table(name = "Messages")
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer messageId;

	@ManyToOne
	@JoinColumn(name = "sender_id", nullable = false)
	@JsonBackReference
	private User sender;

	@ManyToOne
	@JoinColumn(name = "receiver_id", nullable = false)
	@JsonBackReference
	private User receiver;

	@Column(nullable = false)
	private String content;

	@Column(name = "is_read", columnDefinition = "boolean default false")
	private Boolean isRead = false;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now(); // Thiết lập thời gian tạo khi lưu
	}
}
