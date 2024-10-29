package com.moe.music.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Songs")
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "song_id")
    private int songId;

    @NotNull
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String title;

    @Size(max = 50)
    @Column(length = 50)
    private String genre;

    @NotNull
    @Column(nullable = false)
    private short duration;

    @Size(max = 255)
    @Column(name = "cover_image_url", length = 255)
    private String coverImageUrl;

    @Size(max = 255)
    @Column(name = "cover_video_url", length = 255)
    private String coverVideoUrl;

    @Lob
    private String lyrics;

    @NotNull
    @Size(max = 255)
    @Column(name = "file_url", nullable = false, length = 255)
    private String fileUrl;

    @Column(name = "view_count", columnDefinition = "INT DEFAULT 0")
    private int viewCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // Callbacks to automatically set createdAt and updatedAt
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
