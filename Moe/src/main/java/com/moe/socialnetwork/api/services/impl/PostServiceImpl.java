package com.moe.socialnetwork.api.services.impl;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.moe.socialnetwork.api.dtos.PostResponseDTO;
import com.moe.socialnetwork.api.dtos.PostSearchResponseDTO;
import com.moe.socialnetwork.api.services.IPostService;
import com.moe.socialnetwork.common.jpa.AudioJpa;
import com.moe.socialnetwork.common.jpa.ImageJpa;
import com.moe.socialnetwork.common.jpa.PostJpa;
import com.moe.socialnetwork.common.models.Audio;
import com.moe.socialnetwork.common.models.Image;
import com.moe.socialnetwork.common.models.Playlist;
import com.moe.socialnetwork.common.models.Post;
import com.moe.socialnetwork.common.models.PostPlaylist;
import com.moe.socialnetwork.common.models.User;
import com.moe.socialnetwork.common.models.UserPlaylist;
import com.moe.socialnetwork.exception.AppException;

@Service
public class PostServiceImpl implements IPostService {

	private final PostJpa postJPA;
	private final AudioJpa audioJPA;
	private final ImageJpa imageJPA;
	private final CloudinaryServiceImpl cloudinaryService;

	@Value("${max.posts}")
	private int maxPosts;

	public PostServiceImpl(PostJpa postJPA, AudioJpa audioJPA, ImageJpa imageJPA,
			CloudinaryServiceImpl cloudinaryService) {
		this.postJPA = postJPA;
		this.audioJPA = audioJPA;
		this.imageJPA = imageJPA;
		this.cloudinaryService = cloudinaryService;
	}

	@Override
	public List<PostSearchResponseDTO> searchPosts(String keyword) {
		Pageable pageable = PageRequest.of(0, 20);
		List<Post> posts = postJPA.findByKeyword(keyword, pageable);

		if (posts.isEmpty()) {
			throw new AppException("No posts found containing the keyword!", 404);
		}

		List<PostSearchResponseDTO> response = new ArrayList<>();
		for (Post post : posts) {
			response.add(new PostSearchResponseDTO(
					post.getUser().getAvatar(),
					post.getUser().getDisplayName(),
					post.getTitle(),
					String.valueOf(post.getId()),
					post.getVideoUrl()));
		}

		return response;
	}

	@Override
	public void createNewPost(MultipartFile videoFile, List<MultipartFile> imageFile, String title, String description,
			boolean useOtherAudio, Long postId, User user) throws IOException {
		if (title == null || title.isEmpty()) {
			throw new AppException("Content must not be empty.", 400);
		}
		if (useOtherAudio && postId == null) {
			throw new AppException("Post ID must be provided when using other audio.", 400);
		}
		if (videoFile.getSize() > 1024L * 1024 * 1024) { // 1GB = 1024MB = 1024 * 1024 * 1024 bytes hậu tố L khi vượt
															// quá giới hạn của int
			throw new AppException("Video file size exceeds the limit of 1GB.", 400);
		}

		try {
			boolean hasVideo = videoFile != null && !videoFile.isEmpty();
			boolean hasImages = imageFile != null && !imageFile.isEmpty()
					&& !imageFile.get(0).isEmpty(); // Kiểm tra xem phần tử đầu tiên có rỗng không

			if ((!hasVideo && !hasImages) || (hasVideo && hasImages)) {
				throw new AppException("Post must contain either a video or images.", 400);
			}

			Post post = null;
			Audio audio = null;

			if (postId != null) {

				post = postJPA.findById(postId)
						.orElseThrow(() -> new AppException("Post not found", 404));
				audio = audioJPA.findAudioByOwnerPostId(postId);
			}

			Post create = new Post();
			create.setUser(user);
			create.setTitle(title);
			create.setDescription(description);
			create.setAudio(null);

			boolean isUpImage = false;

			if (audio != null) {

				if (hasVideo && !hasImages) {
					if (useOtherAudio) {
						try {
							File videoTemp = cloudinaryService.convertMultipartToFile(videoFile, "video.mp4");
							File audioTemp = cloudinaryService.downloadFileFromCloudinary(audio.getAudioName(),
									"audio.mp3", "video"); // đúng ra ở đây sử dụng type audio nhưng cloudinary nhận
															// audio là video
							File mergedVideo = cloudinaryService.mergeVideoWithAudio(videoTemp, audioTemp);
							String vidPublicUrl = cloudinaryService.uploadVideo(mergedVideo);

							create.setVideoUrl(vidPublicUrl);
						} catch (Exception e) {
							throw new AppException("Failed to process video with audio: " + e.getMessage(), 500);
						}
					} else {
						String vidPublicUrl = cloudinaryService.uploadVideo(videoFile);
						create.setVideoUrl(vidPublicUrl);
					}
					create.setAudio(audio);
					create.setType(Post.PostType.VIDEO);
				} else if (!hasVideo && hasImages) {
					create.setAudio(audio);
					create.setType(Post.PostType.IMAGE);
					isUpImage = true;
				}
			} else {
				if (hasVideo && !hasImages) {
					if (useOtherAudio) {
						try {
							File originalVideo = cloudinaryService.downloadFileFromCloudinary(post.getVideoUrl(),
									"original.mp4", "video");
							File extractedAudio = cloudinaryService.extractAudioFromVideo(originalVideo);
							String audioPublicUrl = cloudinaryService.uploadAudio(extractedAudio);
							File videoTemp = cloudinaryService.convertMultipartToFile(videoFile, "video.mp4");
							File mergedVideo = cloudinaryService.mergeVideoWithAudio(videoTemp, extractedAudio);
							String videoPublicUrl = cloudinaryService.uploadVideo(mergedVideo);

							Audio newAudio = new Audio();
							newAudio.setAudioName(audioPublicUrl);
							newAudio.setOwnerPost(post);
							audioJPA.save(newAudio);
							create.setAudio(newAudio);
							create.setVideoUrl(videoPublicUrl);
						} catch (Exception e) {
							throw new AppException("Failed to process video with extracted audio: " + e.getMessage(),
									500);
						}
					} else {
						create.setAudio(null);
						String vidPublicUrl = cloudinaryService.uploadVideo(videoFile);
						create.setVideoUrl(vidPublicUrl);
					}
					create.setType(Post.PostType.VIDEO); // ✅ Đảm bảo luôn set type
				} else if (!hasVideo && hasImages) {
					if (post != null) {
						try {
							File originalVideo = cloudinaryService.downloadFileFromCloudinary(post.getVideoUrl(),
									"original.mp4", "video");
							File extractedAudio = cloudinaryService.extractAudioFromVideo(originalVideo);
							String audioPublicUrl = cloudinaryService.uploadAudio(extractedAudio);

							Audio newAudio = new Audio();
							newAudio.setAudioName(audioPublicUrl);
							newAudio.setOwnerPost(post);
							audioJPA.save(newAudio);
							create.setAudio(newAudio);
						} catch (Exception e) {
							throw new AppException("Failed to extract audio from video: " + e.getMessage(), 500);
						}
					}
					create.setType(Post.PostType.IMAGE); // ✅ Đảm bảo luôn set type
					isUpImage = true;
				}
			}

			postJPA.save(create);

			if (imageFile != null && !imageFile.isEmpty()
					&& !imageFile.get(0).isEmpty()) {
				if (isUpImage) {
					for (MultipartFile image : imageFile) {
						Image imageSave = new Image();
						String imgUrl = cloudinaryService.uploadImage(image);
						imageSave.setPost(create);
						imageSave.setImageName(imgUrl);
						imageJPA.save(imageSave);
					}
				}
			}
		} catch (Exception e) {
			throw new AppException("Error creating new post: " + e.getMessage(), 500);
		}
	}

	@Override
	public void deletePost(Long postId, User user) {
		try {
			Post post = new Post();
			post.softDelete();
			post.setUserDelete(user);
			postJPA.save(post);
		} catch (Exception e) {
			throw new AppException("An error occurred", 500);
		}
	}

	@Override
	public List<PostResponseDTO> getPostList(User user) {
		// 1. Lấy top 25 tagId mà user đã like
		List<Long> tagIds = postJPA.findTopTagIdsUserLiked(user.getId(), PageRequest.of(0, 25));

		// 2. Lấy các post chứa tag này, chưa xem
		List<Post> candidatePosts = tagIds.isEmpty() ? new ArrayList<>()
				: postJPA.findUnviewedPostsByTags(user.getId(), tagIds);

		// 3. Tính điểm và sắp xếp
		List<PostWithScore> scoredPosts = candidatePosts.stream()
				.map(post -> {
					int likes = post.getLikes() != null ? post.getLikes().size() : 0;
					int comments = post.getComments() != null ? post.getComments().size() : 0;
					int views = post.getViews() != null ? post.getViews().size() : 0;
					double hoursSincePosted = Duration.between(post.getCreatedAt(), LocalDateTime.now()).toHours();
					double score = computeScore(likes, comments, views, hoursSincePosted);
					return new PostWithScore(post, score);
				})
				.sorted(Comparator.comparingDouble(PostWithScore::getScore).reversed())
				.collect(Collectors.toList());

		// 4. Lấy 9-18 post đầu tiên
		int limit = Math.max(9, Math.min(18, scoredPosts.size()));
		List<Post> result = scoredPosts.stream()
				.limit(limit)
				.map(PostWithScore::getPost)
				.collect(Collectors.toList());

		// 5. Nếu chưa đủ, lấy thêm post chưa xem bất kỳ (lọc trùng)
		Set<Long> seenPostIds = result.stream().map(Post::getId).collect(Collectors.toSet());
		if (result.size() < 18) {
			List<Post> moreUnviewed = postJPA.findRandomUnviewedPosts(user.getId(),
					PageRequest.of(0, 18 - result.size()));
			for (Post p : moreUnviewed) {
				if (!seenPostIds.contains(p.getId())) {
					result.add(p);
					seenPostIds.add(p.getId());
				}
				if (result.size() >= 18)
					break;
			}
		}

		// 6. Nếu vẫn chưa đủ, lấy post ngẫu nhiên (lọc trùng)
		if (result.size() < 18) {
			List<Post> randomPosts = postJPA.findRandomPosts(PageRequest.of(0, 18 - result.size()));
			for (Post p : randomPosts) {
				if (!seenPostIds.contains(p.getId())) {
					result.add(p);
					seenPostIds.add(p.getId());
				}
				if (result.size() >= 18)
					break;
			}
		}

		// 7. Chuyển sang DTO
		return result.stream().limit(18).map(post -> this.toPostResponse(post, user)).collect(Collectors.toList());
	}

	private PostResponseDTO toPostResponse(Post post, User user) {
		PostResponseDTO dto = new PostResponseDTO();
		dto.setUserCode(String.valueOf(user.getCode()));
		dto.setPostCode(String.valueOf(post.getCode()));
		dto.setCreatedAt(post.getCreatedAt().toString());

		dto.setUserAvatar(post.getUser().getAvatar());
		dto.setUserDisplayName(post.getUser().getDisplayName());
		dto.setUserName(post.getUser().getUsername());

		dto.setPostType(post.getType().toString());
		dto.setVideoUrl(post.getVideoUrl());

		dto.setTitle(post.getTitle());
		dto.setDescription(post.getDescription());

		List<String> imageUrls = new ArrayList<>();
		for (Image image : post.getImages()) {
			imageUrls.add(image.getImageName());
		}
		dto.setImageUrls(imageUrls);
		dto.setLikeCount(String.valueOf(post.getLikes().size()));
		dto.setCommentCount(String.valueOf(post.getComments().size()));
		// Kiểm tra xem post này đã nằm trong bất kỳ playlist nào của user chưa
		Boolean isInAnyPlaylist = false;
		for (UserPlaylist userPlaylist : user.getUserPlaylists()) {
			Playlist playlist = userPlaylist.getPlaylist();
			if (playlist != null) {
				for (PostPlaylist postPlaylist : playlist.getPostPlaylists()) {
					if (postPlaylist.getPost().getId().equals(post.getId())) {
						isInAnyPlaylist = true;
						break;
					}
				}
			}
		}
		dto.setIsAddPlaylist(isInAnyPlaylist);

		// Kiểm tra nếu audio không null trước khi truy cập các thuộc tính của nó
		if (post.getAudio() != null && post.getAudio().getOwnerPost() != null) {
			dto.setAudioUrl(post.getAudio().getAudioName());
			dto.setAudioOwnerAvatar(post.getAudio().getOwnerPost().getUser().getAvatar());
			dto.setAudioOwnerDisplayName(post.getAudio().getOwnerPost().getUser().getDisplayName());
			dto.setAudioCode(String.valueOf(post.getAudio().getId()));
		} else {
			dto.setAudioUrl(null);
			dto.setAudioOwnerAvatar(null);
			dto.setAudioOwnerDisplayName(null);
			dto.setAudioCode(null);
		}

		dto.setComments(null);
		return dto;
	}

	public double computeScore(int likes, int comments, int views, double hoursSincePosted) {
		double l = Math.log(likes + 1);
		double c = Math.log(comments + 1);
		double v = Math.log(views + 1);

		double w1 = 2.0, w2 = 3.0, w3 = 1.0;
		double raw = w1 * l + w2 * c + w3 * v;

		double decayRate = 0.99; // Giảm dần theo thời gian
		double decay = Math.pow(decayRate, hoursSincePosted);

		return raw * decay;
	}

	private static class PostWithScore {
		private final Post post;
		private final double score;

		public PostWithScore(Post post, double score) {
			this.post = post;
			this.score = score;
		}

		public Post getPost() {
			return post;
		}

		public double getScore() {
			return score;
		}
	}

}
