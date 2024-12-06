package com.moe.music.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.moe.music.jpa.PostJPA;
import com.moe.music.model.Post;

@Service
public class PostService {

	@Autowired
	private PostJPA postJPA;

	@Value("${max.posts}")
	private int maxPosts;

	public List<Post> getHotPosts(Integer userId) {
		Pageable pageable = PageRequest.of(0, 3);
		return postJPA.findHotPosts(userId, pageable);
	}

	public List<Post> getPersonalizedPosts(Integer userId) {
		Pageable pageable = PageRequest.of(0, 9);
		return postJPA.findPersonalizedPosts(userId, pageable);
	}

	public List<Post> getExplorationPosts(Integer userId) {
		Pageable pageable = PageRequest.of(0, 3);
		return postJPA.findExplorationPosts(userId, pageable);
	}

	public List<Post> getRandomPosts(Integer userId) {
		Pageable pageable = PageRequest.of(0, 3);
		return postJPA.findUnseenAndUnlikedPostsRandomly(userId, pageable);
	}

	public List<Post> getPostsFromFollowedUsers(Integer userId) {
		Pageable pageable = PageRequest.of(0, 6);
		return postJPA.findPostsFromFollowedUsers(userId, pageable);
	}

	public List<Post> getUnseenOrUnlikedPosts(Integer userId) {
		Pageable pageable = PageRequest.of(0, 6);
		return postJPA.findUnseenOrUnlikedPosts(userId, pageable);
	}

	public List<Post> getPost(Integer userId) {
		// Bước 1: Lấy các bài theo cơ chế ban đầu
		List<Post> hotPosts = getHotPosts(userId);
		List<Post> personalizedPosts = getPersonalizedPosts(userId);
		List<Post> explorationPosts = getExplorationPosts(userId);

		// Kết hợp các bài đăng đầu tiên
		Set<Post> finalPostsSet = new HashSet<>();
		finalPostsSet.addAll(hotPosts);
		finalPostsSet.addAll(personalizedPosts);
		finalPostsSet.addAll(explorationPosts);

		// Bước 2: Kiểm tra số lượng bài đăng
		int remaining = maxPosts - finalPostsSet.size();

		// Nếu số bài đăng hiện có < 18 và >= 12, bổ sung thêm
		if (remaining > 0) {
			// Bổ sung từ các cơ chế tiếp theo
			List<Post> followedPosts = getPostsFromFollowedUsers(userId);
			List<Post> unseenOrUnlikedPosts = getUnseenOrUnlikedPosts(userId);

			// Chọn ra các bài từ các cơ chế này cho đến khi đủ số bài
			List<Post> additionalPosts = new ArrayList<>();
			if (followedPosts.size() >= remaining) {
				additionalPosts.addAll(followedPosts.subList(0, remaining));
			} else {
				additionalPosts.addAll(followedPosts);
				remaining -= followedPosts.size();

				if (remaining > 0 && unseenOrUnlikedPosts.size() >= remaining) {
					additionalPosts.addAll(unseenOrUnlikedPosts.subList(0, remaining));
				} else {
					additionalPosts.addAll(unseenOrUnlikedPosts);
					remaining -= unseenOrUnlikedPosts.size();
				}
			}

			// Thêm bài bổ sung vào finalPostsSet
			finalPostsSet.addAll(additionalPosts);
		}

		// Bước 3: Nếu vẫn chưa đủ số bài (dưới 18 bài), bổ sung bài ngẫu nhiên
		if (finalPostsSet.size() < maxPosts) {
			int remainingForRandom = maxPosts - finalPostsSet.size();
			List<Post> randomPosts = getRandomPosts(userId);

			// Thêm bài ngẫu nhiên vào finalPostsSet
			finalPostsSet.addAll(randomPosts.subList(0, Math.min(remainingForRandom, randomPosts.size())));
		}

		// Chuyển đổi từ Set sang List
		return finalPostsSet.stream().collect(Collectors.toList());
	}
}
