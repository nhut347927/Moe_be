package com.moe.music.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PostTags")
public class PostTag {

    @EmbeddedId
    private PostTagId id;

    // Relationships
    @ManyToOne
    @MapsId("postId") // Tham chiếu đến postId trong PostTagId
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @MapsId("tagId") // Tham chiếu đến tagId trong PostTagId
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    // Callback to automatically set postId and tagId in id
    @PrePersist
    protected void onCreate() {
        this.id = new PostTagId(post.getPostId(), tag.getTagId());
    }

    // Composite Key Class
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostTagId implements java.io.Serializable {
        private int postId;
        private int tagId;

        @Override
        public int hashCode() {
            return java.util.Objects.hash(postId, tagId);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PostTagId)) return false;
            PostTagId that = (PostTagId) o;
            return postId == that.postId && tagId == that.tagId;
        }
    }
}
