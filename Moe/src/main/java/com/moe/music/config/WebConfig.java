package com.moe.music.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình Web MVC cho ứng dụng, bao gồm cấu hình CORS.
 * 
 * CORS (Cross-Origin Resource Sharing) cho phép quản lý việc truy cập từ các
 * nguồn khác nhau đến tài nguyên của ứng dụng.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	/**
	 * Thêm cấu hình CORS cho tất cả các endpoint trong ứng dụng.
	 * 
	 * @param registry Đối tượng CorsRegistry để cấu hình CORS
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**") // Cho phép truy cập tất cả các endpoint
				.allowedOriginPatterns("http://localhost:3000") // Cho phép yêu cầu từ nguồn cụ thể
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Các phương thức HTTP được cho phép
				.allowedHeaders("*") // Cho phép tất cả các header trong yêu cầu
				.exposedHeaders("Authorization") // Expose header "Authorization" cho client
				.allowCredentials(true) // Cho phép gửi thông tin xác thực như cookies
				.maxAge(3600); // Thời gian cache cho CORS preflight requests (tính bằng giây)
	}
}
