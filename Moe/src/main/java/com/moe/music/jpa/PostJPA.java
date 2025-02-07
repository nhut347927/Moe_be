package com.moe.music.jpa;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.moe.music.model.Post;

public interface PostJPA extends JpaRepository<Post, Integer> {
	@Query("SELECT p FROM Post p " + "LEFT JOIN Like l ON l.post = p AND l.user.id = :userId "
			+ "LEFT JOIN View v ON v.post = p AND v.user.id = :userId "
			+ "WHERE (l.user IS NULL AND v.user IS NULL) "
			+ "ORDER BY (SIZE(p.likes) + SIZE(p.comments) + SIZE(p.views)) DESC, p.createdAt DESC")
	List<Post> findHotPosts(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT DISTINCT p FROM Post p " + "JOIN PostTag pt ON pt.post = p " + "JOIN Tag t ON t = pt.tag "
			+ "WHERE t IN (" + "   SELECT DISTINCT t2 FROM Tag t2 " + "   JOIN PostTag pt2 ON pt2.tag = t2 "
			+ "   JOIN Post p2 ON pt2.post = p2 " + "   WHERE p2 IN ("
			+ "       SELECT l.post FROM Like l WHERE l.user.id = :userId " + "       UNION "
			+ "       SELECT c.post FROM Comment c WHERE c.user.id = :userId " + "       UNION "
			+ "       SELECT ps.post FROM PostPlaylist ps "
			+ "       JOIN Playlist s ON ps.playlist = s WHERE s.user.id = :userId " + "       UNION "
			+ "       SELECT p3 FROM Post p3 WHERE p3.user.id IN ("
			+ "           SELECT f.followed.id FROM Follower f WHERE f.follower.id = :userId" + "       )"
			+ "   )" + ") " + "AND p NOT IN (" + "   SELECT l.post FROM Like l WHERE l.user.id = :userId "
			+ "   UNION " + "   SELECT v.post FROM View v WHERE v.user.id = :userId" + ") "
			+ "ORDER BY p.createdAt DESC")
	List<Post> findPersonalizedPosts(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT p FROM Post p " + "JOIN Follower f ON f.followed.id = p.user.id "
			+ "LEFT JOIN Like l ON l.post = p AND l.user.id = :userId "
			+ "LEFT JOIN View v ON v.post = p AND v.user.id = :userId "
			+ "WHERE (l.user IS NULL AND v.user IS NULL) " + "ORDER BY p.createdAt DESC")
	List<Post> findExplorationPosts(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT p FROM Post p " + "LEFT JOIN Like l ON l.post = p AND l.user.id = :userId "
			+ "LEFT JOIN View v ON v.post = p AND v.user.id = :userId "
			+ "WHERE (l.user IS NULL AND v.user IS NULL) " + "ORDER BY RANDOM()")
	List<Post> findUnseenAndUnlikedPostsRandomly(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT p FROM Post p " + "LEFT JOIN Like l ON l.post = p AND l.user.id = :userId "
			+ "LEFT JOIN View v ON v.post = p AND v.user.id = :userId "
			+ "WHERE (l.user IS NULL AND v.user IS NULL) " + "ORDER BY p.createdAt DESC")
	List<Post> findUnseenOrUnlikedPosts(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT p FROM Post p " + "JOIN Follower f ON f.followed.id = p.user.id "
			+ "LEFT JOIN Like l ON l.post = p AND l.user.id = :userId "
			+ "LEFT JOIN View v ON v.post = p AND v.user.id = :userId " + "WHERE f.follower.id = :userId "
			+ "AND (l.user IS NULL AND v.user IS NULL) " + "ORDER BY p.createdAt DESC")
	List<Post> findPostsFromFollowedUsers(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT p FROM Post p " + "ORDER BY RANDOM()")
	List<Post> findRandomPosts(@Param("userId") Long userId, Pageable pageable);

}
