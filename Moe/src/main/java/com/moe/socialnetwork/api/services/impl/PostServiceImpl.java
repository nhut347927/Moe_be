package com.moe.socialnetwork.api.services.impl;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import com.moe.socialnetwork.api.dtos.PostCreateRepuestDTO;
import com.moe.socialnetwork.api.dtos.PostResponseDTO;
import com.moe.socialnetwork.api.dtos.PostSearchResponseDTO;
import com.moe.socialnetwork.api.dtos.PostCreateRepuestDTO.FFmpegMergeParams;
import com.moe.socialnetwork.api.services.IFFmpegService;
import com.moe.socialnetwork.api.services.IPostService;
import com.moe.socialnetwork.common.jpa.AudioJpa;
import com.moe.socialnetwork.common.jpa.ImageJpa;
import com.moe.socialnetwork.common.jpa.PostJpa;
import com.moe.socialnetwork.common.jpa.TagJpa;
import com.moe.socialnetwork.common.models.Audio;
import com.moe.socialnetwork.common.models.Image;
import com.moe.socialnetwork.common.models.Playlist;
import com.moe.socialnetwork.common.models.Post;
import com.moe.socialnetwork.common.models.PostPlaylist;
import com.moe.socialnetwork.common.models.PostTag;
import com.moe.socialnetwork.common.models.Tag;
import com.moe.socialnetwork.common.models.User;
import com.moe.socialnetwork.common.models.UserPlaylist;
import com.moe.socialnetwork.exception.AppException;

@Service
public class PostServiceImpl implements IPostService {

	private final PostJpa postJPA;
	private final IFFmpegService ffmpegService;
	private final TagJpa tagJPA;
	private final AudioJpa audioJPA;
	private final ImageJpa imageJPA;
	private final CloudinaryServiceImpl cloudinaryService;

	public PostServiceImpl(PostJpa postJPA, TagJpa tagJPA, AudioJpa audioJPA, ImageJpa imageJPA,
			CloudinaryServiceImpl cloudinaryService, IFFmpegService ffmpegService) {
		this.postJPA = postJPA;
		this.tagJPA = tagJPA;
		this.audioJPA = audioJPA;
		this.imageJPA = imageJPA;
		this.cloudinaryService = cloudinaryService;
		this.ffmpegService = ffmpegService;
	}

	public Boolean createNewPost(PostCreateRepuestDTO dto, User user) {
		// 1. Create post
		Post post = new Post();
		post.setUser(user);
		post.setTitle(dto.getTitle());
		post.setDescription(dto.getDescription());
		post.setType("VID".equals(dto.getPostType()) ? Post.PostType.VIDEO : Post.PostType.IMAGE);
		post.setIsDeleted(false);
		post.setCreatedAt(LocalDateTime.now());
		post.setVideoThumbnail(String.valueOf(dto.getVideoThumbnail() != null ? dto.getVideoThumbnail() : 0));

		// 2. Handle tagList
		if (dto.getTagList() != null) {
			for (String tagName : dto.getTagList()) {
				Tag tag = tagJPA.findByName(tagName).orElseGet(() -> {
					Tag newTag = new Tag();
					newTag.setName(tagName);
					return tagJPA.save(newTag);
				});
				PostTag postTag = new PostTag();
				postTag.setPost(post);
				postTag.setTag(tag);
				post.getPostTags().add(postTag);
			}
		}

		// 3. Handle postType
		if ("VID".equals(dto.getPostType())) {
			// 3.1 No extra audio
			if (Boolean.FALSE.equals(dto.getIsUseOtherAudio())) {
				post.setVideoUrl(dto.getVideoPublicId());
				post.setVideoThumbnail(String.valueOf(dto.getVideoThumbnail()));
				File videoFile = ffmpegService.downloadFileFromCloudinary(dto.getVideoPublicId(),
									"audio.mp3", "video"); // đúng ra ở đây sử dụng type audio nhưng cloudinary nhận
															// audio là video
				try {
					File audioFile = ffmpegService.extractAudioFromVideo(videoFile);
					String audioPublicId = cloudinaryService.uploadAudio(audioFile);
					Audio audioPost = new Audio();
					audioPost.setAudioName(audioPublicId);
					audioPost.setOwnerPost(post);
					audioJPA.save(audioPost);
				} catch (java.io.IOException e) {
					throw new AppException("Failed to extract audio from video: " + e.getMessage(), 500);
				}
			} else {
				// 3.2 Has extra audio
				if (dto.getFfmpegMergeParams() == null || dto.getAudioCode() == null) {
					throw new AppException(
							"ffmpegCommand and postCodeByAudio must not be null when using external audio", 400);
				}
				// Load audio post
				Audio audioPost = audioJPA.findAudioByCode(dto.getAudioCode())
						.orElseThrow(() -> new AppException("Audio post not found with provided postCode", 404));
				post.setAudio(audioPost);

				// audio code -> audioPublicId
				FFmpegMergeParams ffmpegParams = dto.getFfmpegMergeParams();
				ffmpegParams.setAudioPublicId(audioPost.getAudioName());
				ffmpegParams.setVideoPublicId(dto.getVideoPublicId());

				// Mock FFmpeg processing logic
				try {
					String vidPublicId = ffmpegService.mergeAndUpload(ffmpegParams);
					String oldVidPublicId = post.getVideoUrl();
					post.setVideoUrl(vidPublicId);
					cloudinaryService.deleteFile(oldVidPublicId);
				} catch (Exception e) {
					throw new AppException("Failed to process video with audio: " + e.getMessage(), 500);
				}
			}

		} else if ("IMG".equals(dto.getPostType())) {
			if (dto.getAudioCode() == null) {
				throw new AppException("postCodeByAudio is required for image posts", 400);
			}

			// Set audio from another post
			Audio audioPost = audioJPA.findAudioByCode(dto.getAudioCode())
					.orElseThrow(() -> new AppException("Audio post not found with provided postCode", 404));
			post.setAudio(audioPost);

			// Save images

			if (dto.getImgList() != null && !dto.getImgList().isEmpty()) {
				for (String imgName : dto.getImgList()) {
					Image image = new Image();
					image.setPost(post);
					image.setImageName(imgName);
					imageJPA.save(image);
				}
			} else {
				throw new AppException("imgList must not be empty for image posts", 400);
			}

		}

		// 4. Save post
		postJPA.save(post);
		return true;
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
