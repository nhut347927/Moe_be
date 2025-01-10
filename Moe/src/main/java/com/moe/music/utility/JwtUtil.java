package com.moe.music.utility;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.moe.music.exception.AppException;
import com.moe.music.jpa.UserJPA;
import com.moe.music.model.User;
import com.moe.music.service.TokenService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtUtil {

	private final TokenService tokenService;

	@Autowired
	public JwtUtil(TokenService tokenService, UserJPA userJPA) {
		this.tokenService = tokenService;
	}

	public List<String> extractPermissions(String token) {
		Claims claims = tokenService.extractAllClaims(token);
		List<?> rawRoles = claims.get("roles", List.class);

		return rawRoles.stream().filter(role -> role instanceof String).map(Object::toString)
				.collect(Collectors.toList());
	}

}
