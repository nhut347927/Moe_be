package com.moe.music.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.moe.music.dto.PostRCMRequestDTO;
import com.moe.music.dto.PostSearchResponseDTO;
import com.moe.music.exception.AppException;
import com.moe.music.interfaces.PostInterface;
import com.moe.music.jpa.AudioJPA;
import com.moe.music.jpa.ImageJPA;
import com.moe.music.jpa.PostJPA;
import com.moe.music.model.Audio;
import com.moe.music.model.Image;
import com.moe.music.model.Post;
import com.moe.music.model.User;
import com.moe.music.utility.TimeAgoFormatter;

@Service
public class PostService implements PostInterface {

	private final PostJPA postJPA;
	private final AudioJPA audioJPA;
	private final ImageJPA imageJPA;
	private final CloudinaryService cloudinaryService;

	@Value("${max.posts}")
	private int maxPosts;

	public PostService(PostJPA postJPA, AudioJPA audioJPA, ImageJPA imageJPA, CloudinaryService cloudinaryService) {
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
					post.getContent(),
					String.valueOf(post.getId()),
					post.getVideo()));
		}

		return response;
	}

	@Override
	public void createNewPost(MultipartFile videoFile, List<MultipartFile> imageFile, String content,
			boolean useOtherAudio, Long postId, User user) throws IOException {
		if (content == null || content.isEmpty()) {
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
			create.setContent(content);
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

							create.setVideo(vidPublicUrl);
						} catch (Exception e) {
							throw new AppException("Failed to process video with audio: " + e.getMessage(), 500);
						}
					} else {
						String vidPublicUrl = cloudinaryService.uploadVideo(videoFile);
						create.setVideo(vidPublicUrl);
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
							File originalVideo = cloudinaryService.downloadFileFromCloudinary(post.getVideo(),
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
							create.setVideo(videoPublicUrl);
						} catch (Exception e) {
							throw new AppException("Failed to process video with extracted audio: " + e.getMessage(),
									500);
						}
					} else {
						create.setAudio(null);
						String vidPublicUrl = cloudinaryService.uploadVideo(videoFile);
						create.setVideo(vidPublicUrl);
					}
					create.setType(Post.PostType.VIDEO); // ✅ Đảm bảo luôn set type
				} else if (!hasVideo && hasImages) {
					if (post != null) {
						try {
							File originalVideo = cloudinaryService.downloadFileFromCloudinary(post.getVideo(),
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

	public List<Post> getHotPosts(Long userId) {
		Pageable pageable = PageRequest.of(0, 3);
		return postJPA.findHotPosts(userId, pageable);
	}

	public List<Post> getPersonalizedPosts(Long userId) {
		Pageable pageable = PageRequest.of(0, 9);
		return postJPA.findPersonalizedPosts(userId, pageable);
	}

	public List<Post> getExplorationPosts(Long userId) {
		Pageable pageable = PageRequest.of(0, 3);
		return postJPA.findExplorationPosts(userId, pageable);
	}

	public List<Post> getRandomPosts(int limit) {
		return postJPA.findRandomPosts(limit);
	}

	public List<Post> getPostsFromFollowedUsers(Long userId) {
		Pageable pageable = PageRequest.of(0, 6);
		return postJPA.findPostsFromFollowedUsers(userId, pageable);
	}

	public List<Post> getUnseenOrUnlikedPosts(Long userId) {
		Pageable pageable = PageRequest.of(0, 6);
		return postJPA.findUnseenOrUnlikedPosts(userId, pageable);
	}

	public List<Post> getPost(Long userId) {
		Set<Post> finalPostsSet = new LinkedHashSet<>(); // Dùng LinkedHashSet để giữ thứ tự bài đăng

		// Bước 1: Lấy các nhóm bài đầu tiên
		finalPostsSet.addAll(getHotPosts(userId));
		finalPostsSet.addAll(getPersonalizedPosts(userId));
		finalPostsSet.addAll(getExplorationPosts(userId));

		// Bước 2: Kiểm tra số lượng bài đăng
		int remaining = maxPosts - finalPostsSet.size();

		if (remaining > 0) {
			List<Post> followedPosts = getPostsFromFollowedUsers(userId);
			List<Post> unseenOrUnlikedPosts = getUnseenOrUnlikedPosts(userId);

			// Loại bỏ bài trùng lặp giữa các giai đoạn
			followedPosts.removeAll(finalPostsSet);
			unseenOrUnlikedPosts.removeAll(finalPostsSet);

			// Bổ sung bài từ danh sách Followed Users trước
			for (Post post : followedPosts) {
				if (finalPostsSet.size() >= maxPosts)
					break;
				finalPostsSet.add(post);
			}

			// Bổ sung bài từ danh sách Unseen Or Unliked
			for (Post post : unseenOrUnlikedPosts) {
				if (finalPostsSet.size() >= maxPosts)
					break;
				finalPostsSet.add(post);
			}
		}

		// Bước 3: Nếu vẫn chưa đủ số bài, bổ sung bài ngẫu nhiên
		if (finalPostsSet.size() < maxPosts) {
			List<Post> randomPosts = getRandomPosts(maxPosts - finalPostsSet.size());

			// Loại bỏ bài trùng lặp giữa các giai đoạn
			randomPosts.removeAll(finalPostsSet);

			// Thêm bài ngẫu nhiên
			for (Post post : randomPosts) {
				if (finalPostsSet.size() >= maxPosts)
					break;
				finalPostsSet.add(post);
			}
		}

		// Đảm bảo tối thiểu 9 bài đăng
		if (finalPostsSet.size() < 9) {
			List<Post> extraRandomPosts = getRandomPosts(9 - finalPostsSet.size());
			extraRandomPosts.removeAll(finalPostsSet);
			finalPostsSet.addAll(extraRandomPosts);
		}

		return new ArrayList<>(finalPostsSet);
	}

	public List<PostRCMRequestDTO> getPostDetail(User user) {
		List<Post> posts = getPost(user.getId());
		List<PostRCMRequestDTO> response = new ArrayList<>();
		
		for (Post post : posts) {
			PostRCMRequestDTO dto = new PostRCMRequestDTO();
			dto.setUserId(String.valueOf(user.getId()));
			dto.setPostId(String.valueOf(post.getId()));
			dto.setCreatedAt(TimeAgoFormatter.formatTimeAgo(post.getCreatedAt()));
			dto.setUserAvatar(post.getUser().getAvatar());
			dto.setUserDisplayName(post.getUser().getDisplayName());
			dto.setPostType(post.getType().toString());
			dto.setVideoUrl(post.getVideo());
	
			List<String> imageUrls = new ArrayList<>();
			for (Image image : post.getImages()) {
				imageUrls.add(image.getImageName());
			}
			dto.setImageUrls(imageUrls);
			dto.setCaption(post.getContent());
			dto.setLikeCount(String.valueOf(post.getLikes().size()));
			dto.setCommentCount(String.valueOf(post.getComments().size()));
			dto.setPlaylistCount(String.valueOf(post.getPostPlaylists().size()));
	
			// Kiểm tra nếu audio không null trước khi truy cập các thuộc tính của nó
			if (post.getAudio() != null && post.getAudio().getOwnerPost() != null) {
				dto.setAudioUrl(post.getAudio().getAudioName());
				dto.setAudioOwnerAvatar(post.getAudio().getOwnerPost().getUser().getAvatar());
				dto.setAudioOwnerName(post.getAudio().getOwnerPost().getUser().getDisplayName());
				dto.setAudioId(String.valueOf(post.getAudio().getId()));
			} else {
				dto.setAudioOwnerAvatar(null);
				dto.setAudioOwnerName(null);
				dto.setAudioId(null);
			}
	
			dto.setComments(null);
			response.add(dto);
		}
		return response;
	}
	
}
