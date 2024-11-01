package com.moe.music.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.moe.music.exception.AppException;
import com.moe.music.jpa.UserJPA;
import com.moe.music.model.User;
import com.moe.music.service.TokenService;
import com.moe.music.service.UserService;

@Component
public class JwtUtil {

	private final TokenService tokenService;
	private final UserJPA userJPA;

	@Autowired
	public JwtUtil(TokenService tokenService, UserJPA userJPA) {
		this.tokenService = tokenService;
		this.userJPA = userJPA;
	}

	/**
	 * Trích xuất và kiểm tra token từ authHeader.
	 *
	 * @param authHeader Header chứa token
	 * @return Đối tượng User tương ứng với token
	 * @throws AppException nếu có lỗi trong quá trình xác thực
	 */
	public User getUserFromAuthHeader(String authHeader) throws AppException {
		// Kiểm tra xem header có hợp lệ hay không
		if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
			throw new AppException("Token không hợp lệ. Vui lòng cung cấp token hợp lệ.",
					HttpStatus.UNAUTHORIZED.value());
		}

		// Trích xuất token từ header substring bỏ "Bearer " 7 kí tự
		String token = authHeader.substring(7);

		// Kiểm tra tính hợp lệ của token
		if (!tokenService.validateJwtToken(token)) {
			throw new AppException("Token đã hết hạn hoặc không hợp lệ.", HttpStatus.UNAUTHORIZED.value());
		}

		// Lấy tên người dùng từ token
		String email = tokenService.getEmailFromJwtToken(token);

		// Tìm kiếm người dùng trong hệ thống
		User user = userJPA.findByEmail(email);
		if (user == null) {
			throw new AppException("Không tìm thấy người dùng với email: " + email, HttpStatus.NOT_FOUND.value());
		}

		return user;
	}
}
