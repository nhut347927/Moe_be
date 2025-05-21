package com.moe.socialnetwork.util;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.moe.socialnetwork.auth.services.impl.TokenServiceImpl;
import com.moe.socialnetwork.common.repositories.UserRepository;

import io.jsonwebtoken.Claims;

@Component
public class JwtUtil {

	private final TokenServiceImpl tokenService;

	public JwtUtil(TokenServiceImpl tokenService, UserRepository userJPA) {
		this.tokenService = tokenService;
	}

	public List<String> extractPermissions(String token) {
		Claims claims = tokenService.extractAllClaims(token);
		List<?> rawRoles = claims.get("roles", List.class);

		return rawRoles.stream().filter(role -> role instanceof String).map(Object::toString)
				.collect(Collectors.toList());
	}

}
