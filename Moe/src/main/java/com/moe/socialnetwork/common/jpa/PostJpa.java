package com.moe.socialnetwork.common.jpa;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.moe.socialnetwork.common.models.Post;

public interface PostJpa extends JpaRepository<Post, Long> {

	@Query("SELECT p FROM Post p JOIN p.user u LEFT JOIN p.audio a WHERE p.isDeleted = false AND p.type = 'VIDEO' AND a IS NULL AND (p.title LIKE %:keyword% OR u.displayName LIKE %:keyword%)")
	List<Post> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

  // Lấy top 25 tagId mà user đã like
    @Query("SELECT t.id FROM Tag t JOIN t.postTags pt JOIN pt.post p JOIN p.likes l WHERE l.user.id = :userId GROUP BY t.id ORDER BY COUNT(l.id) DESC")
    List<Long> findTopTagIdsUserLiked(@Param("userId") Long userId, Pageable pageable);

    // Lấy các post chứa tag, chưa xem
    @Query("SELECT DISTINCT p FROM Post p JOIN p.postTags pt WHERE pt.tag.id IN :tagIds AND p.id NOT IN (SELECT v.post.id FROM View v WHERE v.user.id = :userId) AND p.isDeleted = false")
    List<Post> findUnviewedPostsByTags(@Param("userId") Long userId, @Param("tagIds") List<Long> tagIds);

    // Lấy post chưa xem bất kỳ
    @Query("SELECT p FROM Post p WHERE p.id NOT IN (SELECT v.post.id FROM View v WHERE v.user.id = :userId) AND p.isDeleted = false ORDER BY FUNCTION('RAND')")
    List<Post> findRandomUnviewedPosts(@Param("userId") Long userId, Pageable pageable);

    // Lấy post ngẫu nhiên
    @Query("SELECT p FROM Post p WHERE p.isDeleted = false ORDER BY FUNCTION('RAND')")
    List<Post> findRandomPosts(Pageable pageable);

}
